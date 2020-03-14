package samples.plain.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.naming.Name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import samples.plain.dao.PersonDao;
import samples.plain.domain.Person;
import samples.utils.HtmlRowLdapTreeVisitor;
import samples.utils.LdapTree;
import samples.utils.LdapTreeBuilder;

/**
 * Default controller.
 * 
 * @author Mattias Hellborg Arthursson
 */
@Controller
public class DefaultController {

	@Autowired
	private LdapTreeBuilder ldapTreeBuilder;

	@Autowired
	private PersonDao personDao;

	@RequestMapping("/welcome.do")
	public void welcomeHandler() {
	}

	@RequestMapping("/showTree.do")
	public ModelAndView showTree() {
		LdapTree ldapTree = ldapTreeBuilder.getLdapTree(LdapUtils.emptyLdapName());
		HtmlRowLdapTreeVisitor visitor = new PersonLinkHtmlRowLdapTreeVisitor();
		ldapTree.traverse(visitor);
		return new ModelAndView("showTree", "rows", visitor.getRows());
	}

	@RequestMapping("/addPerson.do")
	public String addPerson() {
		Person person = getPerson();

		personDao.create(person);
		return "redirect:/showTree.do";
	}

//	@RequestMapping("/updatePhoneNumber.do")
//	public String updatePhoneNumber() {
//		Person person = personDao.findByPrimaryKey("Sweden", "company1", "John Doe");
//		person.setPhone(StringUtils.join(new String[] { person.getPhone(), "0" }));
//
//		personDao.update(person);
//        return "redirect:/showTree.do";
//	}

	@RequestMapping("/removePerson.do")
	public String removePerson() {
		Person person = getPerson();

		personDao.delete(person);
        return "redirect:/showTree.do";
	}

	@RequestMapping("/showPerson.do")
	public ModelMap showPerson(String country, String company, String fullName) {
		Person person = personDao.findByPrimaryKey(country, company, fullName);
		return new ModelMap("person", person);
	}

	private Person getPerson() {
		Person person = new Person();
		person.setFullName("John Doe");
		person.setLastName("Doe");
		person.setCompany("company1");
		person.setCountry("Sweden");
		person.setDescription("Test user");
		return person;
	}

	/**
	 * Generates appropriate links for person leaves in the tree.
	 * 
	 * @author Mattias Hellborg Arthursson
	 */
	private static final class PersonLinkHtmlRowLdapTreeVisitor extends HtmlRowLdapTreeVisitor {
		@Override
		protected String getLinkForNode(DirContextOperations node) {
			String[] objectClassValues = node.getStringAttributes("objectClass");
			if (containsValue(objectClassValues, "person")) {
				Name dn = node.getDn();
				String country = encodeValue(LdapUtils.getStringValue(dn, "c"));
				String company = encodeValue(LdapUtils.getStringValue(dn, "ou"));
				String fullName = encodeValue(LdapUtils.getStringValue(dn, "cn"));

				return "showPerson.do?country=" + country + "&company=" + company + "&fullName=" + fullName;
			}
			else {
				return super.getLinkForNode(node);
			}
		}

		private String encodeValue(String value) {
			try {
				return URLEncoder.encode(value, "UTF8");
			}
			catch (UnsupportedEncodingException e) {
				// Not supposed to happen
				throw new RuntimeException("Unexpected encoding exception", e);
			}
		}

		private boolean containsValue(String[] values, String value) {
			for (String oneValue : values) {
				if (oneValue.equals(value)) {
					return true;
				}
			}
			return false;
		}
	}

}
