/*
 * Copyright (C) 2008 Wilfred Springer
 * 
 * This file is part of Preon.
 * 
 * Preon is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * 
 * Preon is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Preon; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package nl.flotsam.preon.codec;

import java.lang.reflect.AnnotatedElement;

import nl.flotsam.limbo.Expression;
import nl.flotsam.limbo.Expressions;
import nl.flotsam.preon.Builder;
import nl.flotsam.preon.Codec;
import nl.flotsam.preon.CodecDescriptor;
import nl.flotsam.preon.CodecFactory;
import nl.flotsam.preon.DecodingException;
import nl.flotsam.preon.Resolver;
import nl.flotsam.preon.ResolverContext;
import nl.flotsam.preon.annotation.Bound;
import nl.flotsam.preon.buffer.BitBuffer;
import nl.flotsam.preon.descriptor.DefaultDescriptor;


/**
 * A {@link CodecFactory} capable of creating {@link Codec Codecs} that deal
 * with booleans.
 * 
 * 
 * @author Wilfred Springer
 * 
 */
public class BooleanCodecFactory implements CodecFactory {

    /*
     * (non-Javadoc)
     * @see nl.flotsam.preon.CodecFactory#create(java.lang.reflect.AnnotatedElement, java.lang.Class, nl.flotsam.preon.ResolverContext)
     */
    @SuppressWarnings("unchecked")
    public <T> Codec<T> create(AnnotatedElement metadata, Class<T> type,
            ResolverContext context) {
        if (metadata != null && metadata.isAnnotationPresent(Bound.class)) {
            if (boolean.class.equals(type)) {
                return (Codec<T>) new BooleanCodec(true);
            } else if (Boolean.class.equals(type)) {
                return (Codec<T>) new BooleanCodec(false);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static class BooleanCodec implements Codec<Boolean> {

        private boolean primitive;

        public BooleanCodec(boolean primitive) {
            this.primitive = primitive;
        }

        public Boolean decode(BitBuffer buffer, Resolver resolver,
                Builder builder) throws DecodingException {
            return buffer.readAsBoolean();
        }

        public CodecDescriptor getCodecDescriptor() {
            return new DefaultDescriptor();
        }

        public int getSize(Resolver resolver) {
            return 1;
        }

        public Class<?>[] getTypes() {
            if (primitive) {
                return new Class[] { boolean.class };
            } else {
                return new Class[] { Boolean.class };
            }
        }

        public Expression<Integer, Resolver> getSize() {
            return Expressions.createInteger(1, Resolver.class);
        }

        public Class<?> getType() {
            return Boolean.class;
        }

    }

}
