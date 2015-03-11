package storybook.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.swing.JDialog;

import org.hibernate.Session;

import storybook.SbConstants.PreferenceKey;
import storybook.model.hbn.dao.GenderDAO;
import storybook.model.hbn.dao.GenderDAOImpl;
import storybook.model.hbn.dao.PersonDAO;
import storybook.model.hbn.dao.PersonDAOImpl;
import storybook.model.BookModel;
import storybook.model.DbFile;
import storybook.model.hbn.entity.Category;
import storybook.model.hbn.entity.Gender;
import storybook.model.hbn.entity.Person;
import storybook.model.hbn.entity.Preference;
import storybook.toolkit.PrefUtil;
import storybook.ui.MainFrame;
import storybook.ui.edit.EntityEditor;

import com.github.irobson.jgenderize.GenderizeIoAPI;
import com.github.irobson.jgenderize.client.Genderize;
import com.github.irobson.jgenderize.model.NameGender;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;


/**
 * Utility class used for importing characters from a text file.
 * Goal is to extract as many characters, matched with their
 * genders, parsed from the text.
 * @author Mark Koh
 *
 */
public class CharacterImporter {

	protected PersonDAOImpl persondao;
	protected GenderDAOImpl genderdao;
	
	/**
	 * Creates new form dlgImport
	 * @param parent : parent frame
	 * @param modal : true or false
	 */
	public CharacterImporter(File file,PersonDAOImpl persondao, GenderDAOImpl genderdao) {
		this.persondao = persondao;
		this.genderdao = genderdao;
		
		ArrayList<Person> characters = extractPerson(file);
		for(Person p : characters){
			System.out.println(p.getFirstname());
			System.out.println(p.getLastname());
			System.out.println(p.getGender());
			StringBuffer abbre = new StringBuffer(p.getFirstname());
			abbre.append(p.getLastname());
			p.setAbbreviation(abbre.toString());
			System.out.println(p.getAbbreviation());
			Preference pref2 = PrefUtil.get(PreferenceKey.LAST_OPEN_FILE, "");
			DbFile dbFile = new DbFile(pref2.getStringValue());
			System.out.println(dbFile);
			Random randomgenerator = new Random();
			Category c = new Category(randomgenerator.nextInt(10000),"Central Character");
			p.setCategory(c);
			System.out.println(p.getCategory());
			MainFrame mf = new MainFrame();
			mf.init(dbFile);
			//mf.showEditorAsDialog(p);
			JDialog dg = new JDialog();
			EntityEditor n = new EntityEditor(mf, p, dg );		
		}
	}
	
	public ArrayList<Person> extractPerson(File file) {
		
		//location of the classifier model
		String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";

		ArrayList<Person> result = new ArrayList<Person>();

		Genderize api = GenderizeIoAPI.create();
		
		Gender male = genderdao.findMale();
		Gender female = genderdao.findFemale();
		
		try {
			AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier
					.getClassifier(serializedClassifier);

			String fileContents = IOUtils.slurpFile(file);

			List<Triple<String, Integer, Integer>> list = classifier
					.classifyToCharacterOffsets(fileContents);
			
			//used for preventing duplicated names.
			HashSet<String> existingNames = new HashSet<String>();
			
			for (Triple<String, Integer, Integer> item : list) {
				if (item.first().equals("PERSON")) {
					String namestr = fileContents.substring(item.second(),
							item.third());
					
					//remove line breaks and extra spaces between fn and ln
					namestr = namestr.replace("\n", " ").replace("\r", " ")
							.replaceAll("\\s+", " ").trim();
					if(!existingNames.contains(namestr)){
						existingNames.add(namestr);
						String[] names = namestr.split(" ");
						Person p = new Person();
						p.setFirstname(names[0]);
						if(names.length > 1)
							p.setLastname(names[1]);
						
						NameGender gender = api.getGender(p.getFirstname());	
						if(gender.getGender() != null) {
							if (gender.getGender() == "male")
								p.setGender(male);
							else
								p.setGender(female);
						}
						else {
							p.setGender(randomGenderGuess(male,female));
						}
						
						result.add(p);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Simulate a coin toss for guessing gender
	 * @return
	 */
	private Gender randomGenderGuess(Gender male, Gender female){
		
		Random rand = new Random();
		
		int toss = rand.nextInt(2);
		
		if(toss == 0)
			return male;
	
		return female;
	}

}
