package storybook.importer;

import com.github.irobson.jgenderize.GenderizeIoAPI;
import com.github.irobson.jgenderize.client.Genderize;
import com.github.irobson.jgenderize.model.NameGender;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;
import storybook.model.hbn.dao.GenderDAOImpl;
import storybook.model.hbn.dao.PersonDAOImpl;
import storybook.model.hbn.entity.Gender;
import storybook.model.hbn.entity.Person;
import storybook.toolkit.I18N;
import storybook.toolkit.swing.splash.HourglassSplash;

import java.io.File;
import java.util.*;

import javax.swing.JOptionPane;
import javax.ws.rs.ClientErrorException;


/**
 * Utility class used for importing characters from a text file.
 * Goal is to extract as many characters, matched with their
 * genders, parsed from the text.
 *
 * @author Mark Koh
 */
public class CharacterImporter {
	final static private String SERIALIZED_CLASSIFIER = "classifiers/english.all.3class.distsim.crf.ser.gz";

	final static private int MAX_BATCH = 10;
	
	protected GenderDAOImpl genderDAO;

	/**
	 * Creates a new instance of {@link storybook.importer.CharacterImporter}
	 * using the provided {@link java.io.File} and DAOs
	 *
	 * @param file is the {@link java.io.File} that the characters will be extracted from.
	 * @param genderDAO
	 */
	public CharacterImporter(GenderDAOImpl genderDAO) {
		this.genderDAO = genderDAO;
	}

	public Collection<Person> extractPerson(File file) throws ClientErrorException {

		//location of the classifier model


		Map<String, Person> people = new HashMap<>();

		Genderize api = GenderizeIoAPI.create();

		Gender male = genderDAO.findMale();
		Gender female = genderDAO.findFemale();
		
		List<Person> batch = new ArrayList<Person>();

		try {
			AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(SERIALIZED_CLASSIFIER);

			String fileContents = IOUtils.slurpFile(file);

			List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(fileContents);

			// Used for preventing duplicated names.

			for (int i=0; i< list.size(); i++) {
				Triple<String, Integer, Integer> item = list.get(i);
				if (item.first().equals("PERSON")) {
					String namestr = fileContents.substring(item.second(), item.third());

					//remove line breaks and extra spaces between fn and ln
					namestr = namestr.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ").trim();

					if (!people.containsKey(namestr)) {

						String[] names = namestr.split(" ");
						Person p = new Person();
						p.setFirstname(names[0]);
						if (names.length > 1) {
							p.setLastname(names[names.length - 1]);
						}
						
						//Set the character abbreviation
						StringBuffer abbreviation = new StringBuffer(p.getFirstname().substring(0, 2));
						if (names.length > 1) {
							abbreviation.append(p.getLastname().substring(0,2));
						}
						p.setAbbreviation(abbreviation.toString());
						
						batch.add(p);

						//If we're at the batch size limit or we're at the last item in the list
						//flush the batch and add the people to the DB
						if (batch.size() == MAX_BATCH || i == list.size()-1) {
							
							//Build the name batch to send to the jGenderize API
							String[] namebatch = new String[batch.size()];
							for (int j = 0; j < batch.size();j++){
								namebatch[j] = batch.get(j).getFirstname();
							}
							
							//Hit the API to get the genders
							List<NameGender> genders = null;
							try {
								genders = api.getGenders(namebatch);
							}
							catch (ClientErrorException e){
								//We've reached the API request limit.  Just guess a random gender.
								System.err.println("Reached API Request limit.  No genders determined for batch.");
							}
							
							
							//For each person in the batch, set the gender and add them to the list
							for(int j=0; j < batch.size(); j++){
								Person current = batch.get(j);
								
								//Make sure we actually got genders
								if (genders != null) {
									NameGender gender = genders.get(j);
									if (gender.getGender() != null) {
										if (gender.isMale()) {
											current.setGender(male);
										}
										else {
											current.setGender(female);
										}
									}
								}
								
								//Put the person in the list
								people.put(namestr, current);
							}
							
							//Clear the batch
							batch.clear();
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return people.values();
	}

	/**
	 * Simulate a coin toss for guessing gender
	 *
	 * @return
	 */
	private Gender randomGenderGuess(Gender male, Gender female) {

		Random rand = new Random();

		int toss = rand.nextInt(2);

		if (toss == 0) {
			return male;
		}

		return female;
	}

}
