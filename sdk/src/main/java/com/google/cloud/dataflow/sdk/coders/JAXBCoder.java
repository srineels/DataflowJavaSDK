/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.dataflow.sdk.coders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * A coder for JAXB annotated objects. This coder uses JAXB marshalling/unmarshalling mechanisms
 * to encode/decode the objects. Users must provide the {@code Class} of the JAXB annotated object.
 *
 * @param <T> type of JAXB annotated objects that will be serialized.
 */
public class JAXBCoder<T> extends CustomCoder<T> {
  private static final long serialVersionUID = 0L;
  private final Class<T> jaxbClass;
  private transient Marshaller jaxbMarshaller = null;
  private transient Unmarshaller jaxbUnmarshaller = null;

  public Class<T> getJAXBClass() {
    return jaxbClass;
  }

  private JAXBCoder(Class<T> jaxbClass) {
    this.jaxbClass = jaxbClass;
  }

  /**
   * Create a coder for a given type of JAXB annotated objects.
   *
   * @param jaxbClass the {@code Class} of the JAXB annotated objects.
   */
  public static <T> JAXBCoder<T> of(Class<T> jaxbClass) {
    return new JAXBCoder<>(jaxbClass);
  }

  @Override
  public void encode(T value, OutputStream outStream, Context context)
      throws CoderException, IOException {
    try {
      if (jaxbMarshaller == null) {
        JAXBContext jaxbContext = JAXBContext.newInstance(jaxbClass);
        jaxbMarshaller = jaxbContext.createMarshaller();
      }

      jaxbMarshaller.marshal(value, outStream);
    } catch (JAXBException e) {
      throw new CoderException(e);
    }
  }

  @Override
  public T decode(InputStream inStream, Context context) throws CoderException, IOException {
    try {
      if (jaxbUnmarshaller == null) {
        JAXBContext jaxbContext = JAXBContext.newInstance(jaxbClass);
        jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      }

      @SuppressWarnings("unchecked")
      T obj = (T) jaxbUnmarshaller.unmarshal(inStream);
      return obj;
    } catch (JAXBException e) {
      throw new CoderException(e);
    }
  }
}

