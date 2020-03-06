package com.github.catchitcozucan.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.CLASS)
public @interface MakeStep {
	String description();
	String statusUponSuccess();
	String statusUponFailure();
	Class<?>[] enumStateProvider() default {};
	String sourceEncoding() default "NONE";
}
