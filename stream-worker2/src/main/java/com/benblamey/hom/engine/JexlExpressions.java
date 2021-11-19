package com.benblamey.hom.engine;

import org.apache.commons.jexl3.*;
import org.apache.kafka.streams.kstream.Predicate;
import org.json.simple.JSONObject;

public class JexlExpressions {

    public static Predicate<Object, Object> jexlToPredicate(String jexlExpression) {
        // Create or retrieve an engine
        JexlEngine jexl = new JexlBuilder().create();

        // Create an expression
        //String jexlExp = "foo.innerFoo.bar()";
        JexlExpression e = jexl.createExpression(jexlExpression);

        // Create a context and add data
        JexlContext jc = new MapContext();

        Predicate<Object, Object> predicate = (Object key, Object data) -> {
            jc.set("data", data);
            jc.set("key", key);
            // Now evaluate the expression, getting the result
            // TODO: this is returning a string
            Object result = e.evaluate(jc);

            System.out.println("Parsing: " + data + " with jexl " + jexlExpression + " result: " + result);

            return (Boolean)result;
        };

        return predicate;
    }


}
