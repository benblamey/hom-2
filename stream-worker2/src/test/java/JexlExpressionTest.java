import static org.junit.jupiter.api.Assertions.assertEquals;

import com.benblamey.hom.engine.JexlExpressions;
import org.apache.kafka.streams.kstream.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class JexlExpressionTest {


    @Test
    public void testJexlExpression1() {
        Predicate<Object, Object> objectObjectPredicate = JexlExpressions.jexlToPredicate("data.foo > 42");

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("foo", 100);
        int key = 123;

        boolean test = objectObjectPredicate.test(key, data);
        assertEquals(true, test);
    }

    @Test
    public void testJexlExpression2() {
        Predicate<Object, Object> objectObjectPredicate = JexlExpressions.jexlToPredicate("data.foo < 42");

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("foo", 100);
        int key = 123;

        boolean test = objectObjectPredicate.test(key, data);
        assertEquals(false, test);
    }


}


