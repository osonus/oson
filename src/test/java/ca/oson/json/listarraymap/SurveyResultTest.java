package ca.oson.json.listarraymap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

import ca.oson.json.FieldMapper;
import ca.oson.json.function.Collection2JsonFunction;
import ca.oson.json.support.TestCaseBase;
import ca.oson.json.util.StringUtil;

public class SurveyResultTest extends TestCaseBase {

	   @Test
	   public void testSerializeList() {
		   SurveyResult result = new SurveyResult();
		   result.name = "John Doe";
		   result.questions = new ArrayList();
		   
		   QuestionAnswer answer = new QuestionAnswer();
		   answer.key = "a";
		   answer.question = "a";
		   answer.answer = true;
		   result.questions.add(answer);
		   
		   answer = new QuestionAnswer();
		   answer.key = "b";
		   answer.question = "b";
		   answer.answer = false;
		   result.questions.add(answer);
		   
		   Collection2JsonFunction serializer = (Collection collection) -> {
			   StringBuffer sb = new StringBuffer();
			   int i = 0;
			   for (Object obj: collection) {
				   if (QuestionAnswer.class.isInstance(obj)) {
					   QuestionAnswer a = (QuestionAnswer)obj;
					   if (i > 0) {
						   sb.append(",");
					   }
					   sb.append(StringUtil.doublequote("question" + (++i) + "key") + ":" + a.answer);
				   }
			   }
			   
			   return sb.toString();
		   };
		   
		   FieldMapper fieldMapper = new FieldMapper("questions", SurveyResult.class).setSerializer(serializer).setJsonNoName(true);
		   oson.setFieldMappers(fieldMapper);

		   String expected = "{\"name\":\"John Doe\",\"question1key\":true,\"question2key\":false}";
		   
		   String json = oson.serialize(result);

		   assertEquals(expected, json);
	   }
	   
}


class SurveyResult {
   public String name;
   public List<QuestionAnswer> questions;
}
class QuestionAnswer {
   public String key;
   public String question;
   public boolean answer;
}