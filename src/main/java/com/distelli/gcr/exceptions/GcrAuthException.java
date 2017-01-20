/*
  $Id: $
  @file GcrAuthException.java
  @brief Contains the GcrAuthException.java class

  @author Rahul Singh [rsingh]
  Copyright (c) 2013, Distelli Inc., All Rights Reserved.
*/
package com.distelli.gcr.exceptions;

public class GcrAuthException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public GcrAuthException()
    {

    }
    public GcrAuthException(String message)
    {
        super(message);
    }

    public GcrAuthException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public GcrAuthException(Throwable cause)
    {
        super(cause);
    }
}
