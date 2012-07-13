package com.delcyon.capo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is used to add namespace URIs to the list of URI's that will be processed by the ControlElement architecture. 
 * If a URI on an element is encountered that exists in this list an attempt will be made to run it as a control, or module.   
 * @author jeremiah
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ControlNamespaceURI
{
    String[] URIList();
}
