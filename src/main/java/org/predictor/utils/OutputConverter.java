package org.predictor.utils;

import java.io.Serializable;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

public class OutputConverter implements IStringConverter<TipoEnum>, Serializable{

	private static final long serialVersionUID = 1L;

    @Override
    public TipoEnum convert(String value) {
    	TipoEnum convertedValue = TipoEnum.fromString(value);

        if(convertedValue == null) {
            throw new ParameterException("Value " + value + "can not be converted to OutputEnum. " +
                    "Available values are: locale, yarn.");
        }
        return convertedValue;
    }
}