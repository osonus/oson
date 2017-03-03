package ca.oson.json.domain;

/*
public class AdmissionsApplicationQuestionType {
	private String questionID;
	private String answerText;
	
	public AdmissionsApplicationQuestionType() {
		
	}

	public String getQuestionID() {
		return questionID;
	}

	public void setQuestionID(String questionID) {
		this.questionID = questionID;
	}

	public String getAnswerText() {
		return answerText;
	}

	public void setAnswerText(String answerText) {
		this.answerText = answerText;
	}

}
*/

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for AnswerSelectedCodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AnswerSelectedCodeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="NotSelected"/&gt;
 *     &lt;enumeration value="Selected"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "AnswerSelectedCodeType")
@XmlEnum
enum AnswerSelectedCodeType {

    @XmlEnumValue("NotSelected")
    NOT_SELECTED("NotSelected"),
    @XmlEnumValue("Selected")
    SELECTED("Selected");
    private final String value;

    AnswerSelectedCodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AnswerSelectedCodeType fromValue(String v) {
        for (AnswerSelectedCodeType c: AnswerSelectedCodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}



/**
 * <p>Java class for AllowedAnswersType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AllowedAnswersType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="AllowedAnswerText" type="{urn:org:pesc:core:CoreMain:v1.14.0}AllowedAnswerTextType"/&gt;
 *         &lt;element name="AnswerSelectedCode" type="{urn:org:pesc:core:CoreMain:v1.14.0}AnswerSelectedCodeType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AllowedAnswersType", propOrder = {
    "allowedAnswerText",
    "answerSelectedCode"
})
class AllowedAnswersType {

    @XmlElement(name = "AllowedAnswerText", required = true)
    protected String allowedAnswerText;
    @XmlElement(name = "AnswerSelectedCode")
    @XmlSchemaType(name = "string")
    protected AnswerSelectedCodeType answerSelectedCode;

    /**
     * Gets the value of the allowedAnswerText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAllowedAnswerText() {
        return allowedAnswerText;
    }

    /**
     * Sets the value of the allowedAnswerText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAllowedAnswerText(String value) {
        this.allowedAnswerText = value;
    }

    /**
     * Gets the value of the answerSelectedCode property.
     * 
     * @return
     *     possible object is
     *     {@link AnswerSelectedCodeType }
     *     
     */
    public AnswerSelectedCodeType getAnswerSelectedCode() {
        return answerSelectedCode;
    }

    /**
     * Sets the value of the answerSelectedCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link AnswerSelectedCodeType }
     *     
     */
    public void setAnswerSelectedCode(AnswerSelectedCodeType value) {
        this.answerSelectedCode = value;
    }

}

/**
 * <p>Java class for QuestionRequiredCodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="QuestionRequiredCodeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="Optional"/&gt;
 *     &lt;enumeration value="Required"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "QuestionRequiredCodeType")
@XmlEnum
enum QuestionRequiredCodeType {

    @XmlEnumValue("Optional")
    OPTIONAL("Optional"),
    @XmlEnumValue("Required")
    REQUIRED("Required");
    private final String value;

    QuestionRequiredCodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QuestionRequiredCodeType fromValue(String v) {
        for (QuestionRequiredCodeType c: QuestionRequiredCodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}



/**
 * <p>Java class for QuestionFormatCodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="QuestionFormatCodeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="CheckBox"/&gt;
 *     &lt;enumeration value="DropDownMenu"/&gt;
 *     &lt;enumeration value="NoInput"/&gt;
 *     &lt;enumeration value="OneLineText"/&gt;
 *     &lt;enumeration value="RadioButton"/&gt;
 *     &lt;enumeration value="TextArea"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "QuestionFormatCodeType")
@XmlEnum
enum QuestionFormatCodeType {

    @XmlEnumValue("CheckBox")
    CHECK_BOX("CheckBox"),
    @XmlEnumValue("DropDownMenu")
    DROP_DOWN_MENU("DropDownMenu"),
    @XmlEnumValue("NoInput")
    NO_INPUT("NoInput"),
    @XmlEnumValue("OneLineText")
    ONE_LINE_TEXT("OneLineText"),
    @XmlEnumValue("RadioButton")
    RADIO_BUTTON("RadioButton"),
    @XmlEnumValue("TextArea")
    TEXT_AREA("TextArea");
    private final String value;

    QuestionFormatCodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QuestionFormatCodeType fromValue(String v) {
        for (QuestionFormatCodeType c: QuestionFormatCodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

/**
 * <p>Java class for QuestionAreaCodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="QuestionAreaCodeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="AcademicRecord"/&gt;
 *     &lt;enumeration value="Admissions"/&gt;
 *     &lt;enumeration value="Biographical"/&gt;
 *     &lt;enumeration value="Education"/&gt;
 *     &lt;enumeration value="Employment"/&gt;
 *     &lt;enumeration value="Essay"/&gt;
 *     &lt;enumeration value="ExtraCurricularActivities"/&gt;
 *     &lt;enumeration value="Family"/&gt;
 *     &lt;enumeration value="FinancialAid"/&gt;
 *     &lt;enumeration value="General"/&gt;
 *     &lt;enumeration value="Health"/&gt;
 *     &lt;enumeration value="HonorsAwards"/&gt;
 *     &lt;enumeration value="Housing"/&gt;
 *     &lt;enumeration value="Immigration"/&gt;
 *     &lt;enumeration value="Other"/&gt;
 *     &lt;enumeration value="Personal"/&gt;
 *     &lt;enumeration value="References"/&gt;
 *     &lt;enumeration value="Residency"/&gt;
 *     &lt;enumeration value="Scholarship"/&gt;
 *     &lt;enumeration value="Tests"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "QuestionAreaCodeType")
@XmlEnum
enum QuestionAreaCodeType {

    @XmlEnumValue("AcademicRecord")
    ACADEMIC_RECORD("AcademicRecord"),
    @XmlEnumValue("Admissions")
    ADMISSIONS("Admissions"),
    @XmlEnumValue("Biographical")
    BIOGRAPHICAL("Biographical"),
    @XmlEnumValue("Education")
    EDUCATION("Education"),
    @XmlEnumValue("Employment")
    EMPLOYMENT("Employment"),
    @XmlEnumValue("Essay")
    ESSAY("Essay"),
    @XmlEnumValue("ExtraCurricularActivities")
    EXTRA_CURRICULAR_ACTIVITIES("ExtraCurricularActivities"),
    @XmlEnumValue("Family")
    FAMILY("Family"),
    @XmlEnumValue("FinancialAid")
    FINANCIAL_AID("FinancialAid"),
    @XmlEnumValue("General")
    GENERAL("General"),
    @XmlEnumValue("Health")
    HEALTH("Health"),
    @XmlEnumValue("HonorsAwards")
    HONORS_AWARDS("HonorsAwards"),
    @XmlEnumValue("Housing")
    HOUSING("Housing"),
    @XmlEnumValue("Immigration")
    IMMIGRATION("Immigration"),
    @XmlEnumValue("Other")
    OTHER("Other"),
    @XmlEnumValue("Personal")
    PERSONAL("Personal"),
    @XmlEnumValue("References")
    REFERENCES("References"),
    @XmlEnumValue("Residency")
    RESIDENCY("Residency"),
    @XmlEnumValue("Scholarship")
    SCHOLARSHIP("Scholarship"),
    @XmlEnumValue("Tests")
    TESTS("Tests");
    private final String value;

    QuestionAreaCodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QuestionAreaCodeType fromValue(String v) {
        for (QuestionAreaCodeType c: QuestionAreaCodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}


/**
 * <p>Java class for AdmissionsApplicationQuestionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AdmissionsApplicationQuestionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="QuestionTitle" type="{urn:org:pesc:core:CoreMain:v1.14.0}QuestionTitleType" minOccurs="0"/&gt;
 *         &lt;element name="QuestionID" type="{urn:org:pesc:core:CoreMain:v1.14.0}QuestionIDType" minOccurs="0"/&gt;
 *         &lt;element name="QuestionAreaCode" type="{urn:org:pesc:core:CoreMain:v1.14.0}QuestionAreaCodeType" minOccurs="0"/&gt;
 *         &lt;element name="QuestionRequiredCode" type="{urn:org:pesc:core:CoreMain:v1.14.0}QuestionRequiredCodeType" minOccurs="0"/&gt;
 *         &lt;element name="QuestionFormatCode" type="{urn:org:pesc:core:CoreMain:v1.14.0}QuestionFormatCodeType" minOccurs="0"/&gt;
 *         &lt;element name="QuestionText" type="{urn:org:pesc:core:CoreMain:v1.14.0}QuestionTextType" minOccurs="0"/&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="AllowedAnswers" type="{urn:org:pesc:sector:AdmissionsRecord:v1.3.0}AllowedAnswersType" maxOccurs="50" minOccurs="0"/&gt;
 *           &lt;element name="AnswerText" type="{urn:org:pesc:core:CoreMain:v1.14.0}AnswerTextType" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AdmissionsApplicationQuestionType", propOrder = {
    "questionTitle",
    "questionID",
    "questionAreaCode",
    "questionRequiredCode",
    "questionFormatCode",
    "questionText",
    "allowedAnswers",
    "answerText"
})
public class AdmissionsApplicationQuestionType {

    @XmlElement(name = "QuestionTitle")
    protected String questionTitle;
    @XmlElement(name = "QuestionID")
    protected String questionID;
    @XmlElement(name = "QuestionAreaCode")
    @XmlSchemaType(name = "string")
    protected QuestionAreaCodeType questionAreaCode;
    @XmlElement(name = "QuestionRequiredCode")
    @XmlSchemaType(name = "string")
    protected QuestionRequiredCodeType questionRequiredCode;
    @XmlElement(name = "QuestionFormatCode")
    @XmlSchemaType(name = "string")
    protected QuestionFormatCodeType questionFormatCode;
    @XmlElement(name = "QuestionText")
    protected String questionText;
    @XmlElement(name = "AllowedAnswers")
    protected List<AllowedAnswersType> allowedAnswers;
    @XmlElement(name = "AnswerText")
    protected String answerText;

    /**
     * Gets the value of the questionTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuestionTitle() {
        return questionTitle;
    }

    /**
     * Sets the value of the questionTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuestionTitle(String value) {
        this.questionTitle = value;
    }

    /**
     * Gets the value of the questionID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuestionID() {
        return questionID;
    }

    /**
     * Sets the value of the questionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuestionID(String value) {
        this.questionID = value;
    }

    /**
     * Gets the value of the questionAreaCode property.
     * 
     * @return
     *     possible object is
     *     {@link QuestionAreaCodeType }
     *     
     */
    public QuestionAreaCodeType getQuestionAreaCode() {
        return questionAreaCode;
    }

    /**
     * Sets the value of the questionAreaCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link QuestionAreaCodeType }
     *     
     */
    public void setQuestionAreaCode(QuestionAreaCodeType value) {
        this.questionAreaCode = value;
    }

    /**
     * Gets the value of the questionRequiredCode property.
     * 
     * @return
     *     possible object is
     *     {@link QuestionRequiredCodeType }
     *     
     */
    public QuestionRequiredCodeType getQuestionRequiredCode() {
        return questionRequiredCode;
    }

    /**
     * Sets the value of the questionRequiredCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link QuestionRequiredCodeType }
     *     
     */
    public void setQuestionRequiredCode(QuestionRequiredCodeType value) {
        this.questionRequiredCode = value;
    }

    /**
     * Gets the value of the questionFormatCode property.
     * 
     * @return
     *     possible object is
     *     {@link QuestionFormatCodeType }
     *     
     */
    public QuestionFormatCodeType getQuestionFormatCode() {
        return questionFormatCode;
    }

    /**
     * Sets the value of the questionFormatCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link QuestionFormatCodeType }
     *     
     */
    public void setQuestionFormatCode(QuestionFormatCodeType value) {
        this.questionFormatCode = value;
    }

    /**
     * Gets the value of the questionText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuestionText() {
        return questionText;
    }

    /**
     * Sets the value of the questionText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuestionText(String value) {
        this.questionText = value;
    }

    /**
     * Gets the value of the allowedAnswers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the allowedAnswers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAllowedAnswers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AllowedAnswersType }
     * 
     * 
     */
    public List<AllowedAnswersType> getAllowedAnswers() {
        if (allowedAnswers == null) {
            allowedAnswers = new ArrayList<AllowedAnswersType>();
        }
        return this.allowedAnswers;
    }

    /**
     * Gets the value of the answerText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnswerText() {
        return answerText;
    }

    /**
     * Sets the value of the answerText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnswerText(String value) {
        this.answerText = value;
    }

}