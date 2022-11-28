package com.predic8.membrane.core.openapi;

import com.predic8.membrane.core.openapi.model.*;
import com.predic8.membrane.core.openapi.validators.*;
import org.junit.*;

import java.io.*;
import java.math.*;
import java.util.*;

import static com.predic8.membrane.core.openapi.util.JsonUtil.*;
import static org.junit.Assert.*;


public class ObjectTest {

    OpenAPIValidator validator;

    @Before
    public void setUp() {
        validator = new OpenAPIValidator(getResourceAsStream("/openapi/object.yml"));
    }

    @Test
    public void numberAsObject() {
        ValidationErrors errors = validator.validate(Request.post().path("/object").body(new JsonBody(getNumbers("object",new BigDecimal(7)))));
//        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
    }

    @Test
    public void stringAsObject() {
        ValidationErrors errors = validator.validate(Request.post().path("/object").body(new JsonBody(getStrings("object","Hossa"))));
        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
    }

    @Test
    public void additionalPropertiesTrue() {

        Map nm = new HashMap();
        nm.put("a","foo");
        nm.put("b","bar");

        Map m = new HashMap();
        m.put("additionalPropertiesTrue",nm);

        ValidationErrors errors = validator.validate(Request.post().path("/object").body(mapToJson(m)));
        System.out.println("errors = " + errors);
        assertEquals(0,errors.size());
    }

    @Test
    public void additionalPropertiesFalse() {

        Map nm = new HashMap();
        nm.put("a","foo");
        nm.put("b","bar");

        Map m = new HashMap();
        m.put("additionalPropertiesFalse",nm);

        ValidationErrors errors = validator.validate(Request.post().path("/object").body(mapToJson(m)));
        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
    }

    @Test
    public void additionalPropertiesStringValid() {

        Map nm = new HashMap();
        nm.put("foo", "bar");
        nm.put("unbekannt1","abc");

        Map m = new HashMap();
        m.put("additionalPropertiesString",nm);

        ValidationErrors errors = validator.validate(Request.post().path("/object").body(mapToJson(m)));
        System.out.println("errors = " + errors);
        assertEquals(0,errors.size());
    }

    @Test
    public void additionalPropertiesStringInvalid() {

        Map nm = new HashMap();
        nm.put("foo", "bar");
        nm.put("illegal", 7);
        nm.put("unbekannt1","abc");

        Map m = new HashMap();
        m.put("additionalPropertiesString",nm);

        System.out.println("m = " + m);

        ValidationErrors errors = validator.validate(Request.post().path("/object").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/additionalPropertiesString/illegal",e.getValidationContext().getJSONpointer());
    }

    @Test
    public void additionalPropertiesObjectValid() {

        Map unbekannt = new HashMap();
        unbekannt.put("a","foo");
        unbekannt.put("b",7);

        Map nm = new HashMap();
        nm.put("foo", "bar");
        nm.put("unbekannt",unbekannt);

        Map m = new HashMap();
        m.put("additionalPropertiesComplex",nm);

        ValidationErrors errors = validator.validate(Request.post().path("/object").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(0,errors.size());
    }

    @Test
    public void additionalPropertiesObjectInvalid() {

        Map unbekannt = new HashMap();
        unbekannt.put("a","foo");
        unbekannt.put("b",7);

        Map nm = new HashMap();
        nm.put("foo", "bar");
        nm.put("unbekannt",unbekannt);
        nm.put("illegal",3);

        Map m = new HashMap();
        m.put("additionalPropertiesComplex",nm);

        ValidationErrors errors = validator.validate(Request.post().path("/object").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/additionalPropertiesComplex/illegal",e.getValidationContext().getJSONpointer());
    }

    @Test
    public void minMaxPropertiesTooLessInvalid() {

        Map nm = new HashMap();
        nm.put("a", "bar");

        Map m = new HashMap();
        m.put("minMaxProperties",nm);

        ValidationErrors errors = validator.validate(Request.post().path("/object").body(mapToJson(m)));
        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/minMaxProperties",e.getValidationContext().getJSONpointer());
    }

    @Test
    public void minMaxPropertiesTooMuchInvalid() {

        Map nm = new HashMap();
        nm.put("a", "bar");
        nm.put("b", "bar");
        nm.put("c", "bar");
        nm.put("d", "bar");
        nm.put("e", "bar");
        nm.put("f", "bar");

        Map m = new HashMap();
        m.put("minMaxProperties",nm);

        ValidationErrors errors = validator.validate(Request.post().path("/object").body(mapToJson(m)));
        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/minMaxProperties",e.getValidationContext().getJSONpointer());
    }



    private InputStream getResourceAsStream(String fileName) {
        return this.getClass().getResourceAsStream(fileName);
    }

}