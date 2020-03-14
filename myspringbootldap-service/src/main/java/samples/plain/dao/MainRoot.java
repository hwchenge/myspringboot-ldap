package samples.plain.dao;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.naming.Name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;

import samples.utils.HtmlRowLdapTreeVisitor;
import samples.utils.LdapTree;
import samples.utils.LdapTreeBuilder;

@Service
public class MainRoot {

	@Autowired
	private LdapTreeBuilder ldapTreeBuilder;

	public void main() {
		System.err.print("456");
//		LdapTree ldapTree = ldapTreeBuilder.getLdapTree(LdapUtils.emptyLdapName());
//		HtmlRowLdapTreeVisitor visitor = new PersonLinkHtmlRowLdapTreeVisitor();
//		ldapTree.traverse(visitor);

	}

	/**
	 * Generates appropriate links for person leaves in the tree.
	 * 
	 * @author Mattias Hellborg Arthursson
	 */
	private class PersonLinkHtmlRowLdapTreeVisitor extends HtmlRowLdapTreeVisitor {
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
