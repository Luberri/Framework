package com.itu.demo.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

// Annotation personnalisée utilisable sur les classes
@Retention(RetentionPolicy.RUNTIME) // disponible à l’exécution
@Target(ElementType.TYPE)           // utilisable sur les classes/interfaces
public @interface Controller {
    String value() default "";
}