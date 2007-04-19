/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.processor.DelegateProcessor;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;

/**
 * @version $Revision: 519943 $
 */
public class InterceptorBuilder<E extends Exchange> implements ProcessorFactory<E> {
    private final List<DelegateProcessor<E>> intercepts = new ArrayList<DelegateProcessor<E>>();
	private final FromBuilder<E> parent;
	private FromBuilder<E> target;

	public InterceptorBuilder(FromBuilder<E> parent) {
        this.parent = parent;
	}
	
	@Fluent("interceptor")
	public InterceptorBuilder<E> add(@FluentArg("ref") DelegateProcessor<E> interceptor) {
		intercepts.add(interceptor);
		return this;
	}
	
	@Fluent(callOnElementEnd=true)
    public FromBuilder<E> target() {
        this.target = new FromBuilder<E>(parent);
        return target;
    }

    public Processor<E> createProcessor() throws Exception {
    	
    	// The target is required.
    	if( target == null ) 
    		throw new RuntimeCamelException("target provided.");
    	
    	// Interceptors are optional
    	DelegateProcessor<E> first=null;
    	DelegateProcessor<E> last=null;
        for (DelegateProcessor<E> p : intercepts) {
            if( first == null ) {
            	first = p;
            }
            if( last != null ) {
            	last.setNext(p);
            }
            last = p;
        }
        
        Processor<E> p = target.createProcessor();
        if( last != null ) {
        	last.setNext(p);
        }
        return first == null ? p : first;
    }
}
