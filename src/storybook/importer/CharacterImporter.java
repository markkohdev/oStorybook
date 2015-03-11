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

import java.io.File;
import java.util.*;


/**
 * Utility class used for importing characters from a text file.
 * Goal is to extract as many characters, matched with their
 * genders, parsed from the text.
 *
 * @author Mark Koh
 */
public class CharacterImporter {
	final static private String SERIALIZED_CLASSIFIER = "classifiers/english.all.3class.distsim.crf.ser.gz";

	protected PersonDAOImpl personDAO;
	protected GenderDAOImpl genderDAO;

	/**
	 * Creates a new instance of {@link storybook.importer.CharacterImporter}
	 * using the provided {@link java.io.File} and DAOs
	 *
	 * @param file is the {@link java.io.File} that the characters will be extracted from.
	 * @param personDAO
	 * @param genderDAO
	 */
	public CharacterImporter(PersonDAOImpl personDAO, GenderDAOImpl genderDAO) {
		this.personDAO = personDAO;
		this.genderDAO = genderDAO;
	}

	public Collection<Person> extractPerson(File file) {

		//location of the classifier model


		Map<String, Person> people = new HashMap<>();

		Genderize api = GenderizeIoAPI.create();

		Gender male = genderDAO.findMale();
		Gender female = genderDAO.findFemale();

		try {
			AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(SERIALIZED_CLASSIFIER);

			String fileContents = IOUtils.slurpFile(file);

			List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(fileContents);

			// Used for preventing duplicated names.

			for (Triple<String, Integer, Integer> item : list) {
				if (item.first().equals("PERSON")) {
					String namestr = fileContents.substring(item.second(), item.third());

					//remove line breaks and extra spaces between fn and ln
					namestr = namestr.replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ").trim();

					if (!people.containsKey(namestr)) {

						String[] names = namestr.split(" ");
						Person p = new Person();

						p.setFirstname(names[0]);

						if (names.length > 1) {
							p.setLastname(names[1]);
						}

						NameGender gender = api.getGender(p.getFirstname());
						if (gender.getGender() != null) {
							if (gender.getGender().equals("male")) {
								p.setGender(male);
							}
							else {
								p.setGender(female);
							}
						}
						else {
							p.setGender(randomGenderGuess(male, female));
						}

						people.put(namestr, p);
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
