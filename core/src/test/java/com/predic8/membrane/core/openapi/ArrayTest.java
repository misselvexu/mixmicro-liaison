package com.predic8.membrane.core.openapi;

import com.predic8.membrane.core.openapi.model.*;
import com.predic8.membrane.core.openapi.validators.*;
import org.junit.*;

import java.io.*;
import java.util.*;

import static com.predic8.membrane.core.openapi.util.JsonUtil.*;
import static java.lang.Boolean.*;
import static org.junit.Assert.*;


public class ArrayTest {

    OpenAPIValidator validator;

    @Before
    public void setUp() {
        validator = new OpenAPIValidator(getResourceAsStream("/openapi/array.yml"));
    }

    @Test
    public void noType() {

        Map m = new HashMap();
        m.put("no-type", listWithDifferentTypes());

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
        System.out.println("errors = " + errors);
        assertEquals(0,errors.size());
    }

    @Test
    public void onlyNumbersValid() {

        List l = new ArrayList();
        l.add(7);
        l.add(0.5);
        l.add(10000000);

        Map m = new HashMap();
        m.put("only-numbers", l);

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
        assertEquals(0,errors.size());
    }

    @Test
    public void onlyNumbersInvalid() {

        Map m = new HashMap();
        m.put("only-numbers", listWithDifferentTypes());

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
        System.out.println("errors = " + errors);
        assertEquals(2,errors.size());
    }

// As of 2022-11-20 parser does not support OpenAPI 3.1.0
//    @Test
//    public void prefixedInvalid() {
//
//        List l = Arrays.asList("foo","DE",7,true);
//
//        Map m = new HashMap();
//        m.put("prefixed", l);
//
//        System.out.println("m = " + m);
//
//        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
//        assertEquals(2,errors.size());
//    }

    @Test
    public void minMaxValid() {

        Map m = new HashMap();
        m.put("min-max", Arrays.asList("foo",7));

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
        System.out.println("errors = " + errors);
        assertEquals(0,errors.size());
    }

    @Test
    public void minMaxTooLessInvalid() {

        Map m = new HashMap();
        m.put("min-max", Arrays.asList("foo"));

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/min-max",e.getValidationContext().getJSONpointer());
        assertTrue(e.getMessage().contains("minItems"));
    }

    @Test
    public void minMaxTooManyInvalid() {

        Map m = new HashMap();
        m.put("min-max", Arrays.asList("foo",7,true,8,"bar"));

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/min-max",e.getValidationContext().getJSONpointer());
        assertTrue(e.getMessage().contains("maxItems"));
    }


    @Test
    public void uniqueItemsInvalid() {

        Map m = new HashMap();
        m.put("uniqueItems", Arrays.asList(4,5,2,3,9,1,2,0));

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/uniqueItems",e.getValidationContext().getJSONpointer());
        assertTrue(e.getMessage().contains("2"));
    }

    @Test
    public void uniqueItemsValid() {

        Map m = new HashMap();
        m.put("uniqueItems", Arrays.asList(4,5,2,3,9,1,0));

        ValidationErrors errors = validator.validate(Request.post().path("/array").body(mapToJson(m)));
        assertEquals(0,errors.size());
    }


    private List listWithDifferentTypes() {
        List l = new ArrayList();
        l.add(7);
        l.add("foo");
        l.add(TRUE);
        return l;
    }

    private InputStream getResourceAsStream(String fileName) {
        return this.getClass().getResourceAsStream(fileName);
    }

}