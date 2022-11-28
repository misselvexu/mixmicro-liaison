package com.predic8.membrane.core.openapi;

import com.predic8.membrane.core.openapi.model.*;
import com.predic8.membrane.core.openapi.validators.*;
import org.junit.*;

import java.io.*;
import java.util.*;

import static com.predic8.membrane.core.openapi.util.JsonUtil.*;
import static org.junit.Assert.*;


public class NullableTest {

    OpenAPIValidator validator;

    @Before
    public void setUp() {
        validator = new OpenAPIValidator(getResourceAsStream("/openapi/nullable.yml"));
    }

    @Test
    public void emailNullValid() {

        Map m = new HashMap();
        m.put("email",null);

        ValidationErrors errors = validator.validate(Request.post().path("/composition").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(0,errors.size());
    }

    @Test
    public void addressObjectNullValid() {

        Map m = new HashMap();
        m.put("address",null);

        ValidationErrors errors = validator.validate(Request.post().path("/composition").body(mapToJson(m)));
        System.out.println("errors = " + errors);
        assertEquals(0,errors.size());
    }

    @Test
    public void contactNullableWithoutTypeInvalid() {

        Map m = new HashMap();
        m.put("contact",null);

        ValidationErrors errors = validator.validate(Request.post().path("/composition").body(mapToJson(m)));
        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/contact", e.getValidationContext().getJSONpointer());
        assertTrue(e.getMessage().contains("null"));
    }

    @Test
    public void telefonNotNullableInvalid() {

        Map m = new HashMap();
        m.put("telefon",null);

        ValidationErrors errors = validator.validate(Request.post().path("/composition").body(mapToJson(m)));
//        System.out.println("errors = " + errors);
        assertEquals(1,errors.size());
        ValidationError e = errors.get(0);
        assertEquals("/telefon", e.getValidationContext().getJSONpointer());
        assertTrue(e.getMessage().toLowerCase().contains("null"));
    }


    private InputStream getResourceAsStream(String fileName) {
        return this.getClass().getResourceAsStream(fileName);
    }

}