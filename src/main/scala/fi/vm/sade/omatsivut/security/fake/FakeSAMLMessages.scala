package fi.vm.sade.omatsivut.security.fake

trait FakeSAMLMessages {

  def createSamlBodyWithHetu(hetu: String) = <samlp:AttributeQuery
  xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
  xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol"
  ID="MyMessageId"
  Version="2.0"
  IssueInstant="2006-07-17T20:31:40">
    <saml:Issuer Format="urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName">
      CN=trscavo@uiuc.edu,OU=User,O=NCSA-TEST,C=US
    </saml:Issuer>
    <saml:Subject>
      <saml:NameID Format="urn:oid:1.2.246.21">
        { hetu }
      </saml:NameID>
    </saml:Subject>
    <saml:Attribute NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"
                    Name="urn:oid:2.5.4.42"
                    FriendlyName="givenName">
    </saml:Attribute>
    <saml:Attribute
    NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"
    Name="urn:oid:1.3.6.1.4.1.1466.115.121.1.26"
    FriendlyName="mail">
    </saml:Attribute>
  </samlp:AttributeQuery>.toString.getBytes

  def invalidXMLBody() = <samlp:AttributeQuery></samlp:AttributeQuery>.toString.getBytes

}
