/*
  $Id: $
  @file GcrException.java
  @brief Contains the GcrException.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr.exceptions;

import java.util.List;
import com.distelli.gcr.models.*;

public class GcrException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    private List<GcrError> _errors;

    public GcrException()
    {

    }

    public GcrException(List<GcrError> errors)
    {
        super(getMessage(errors));
        _errors = errors;
    }

    public GcrException(String message)
    {
        super(message);
    }

    public GcrException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public GcrException(Throwable cause)
    {
        super(cause);
    }

    private static String getMessage(List<GcrError> errors)
    {
        if(errors == null || errors.size() == 0)
            return "[]";

        GcrError firstError = errors.get(0);
        String message = "["+firstError.getCode()+"] "+firstError.getMessage();
        if(errors.size() > 1)
            message = message +"("+(errors.size() - 1)+" more error(s)";
        return message;
    }

    public List<GcrError> getErrors()
    {
        return _errors;
    }
}
