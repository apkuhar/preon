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
 * Preon; see the file COPYING. If not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.flotsam.limbo.Expression;
import nl.flotsam.limbo.Expressions;
import nl.flotsam.limbo.util.ClassUtils;
import nl.flotsam.pecia.Contents;
import nl.flotsam.pecia.ParaContents;
import nl.flotsam.preon.Builder;
import nl.flotsam.preon.Codec;
import nl.flotsam.preon.CodecDescriptor;
import nl.flotsam.preon.CodecSelector;
import nl.flotsam.preon.DecodingException;
import nl.flotsam.preon.Resolver;
import nl.flotsam.preon.buffer.BitBuffer;

/**
 * A {@link Codec} that is able to dynamically choose between different types of
 * objects to decode, based on a couple of leading bits.
 * 
 * @author Wilfred Springer
 * @see CodecSelector
 */
public class SwitchingCodec implements Codec<Object> {

    /**
     * The object responsible for picking the right {@link Codec}.
     */
    private CodecSelector selector;

    /**
     * Constructs a new instance.
     * 
     * @param selector
     *            The object responsible for picking the right {@link Codec}.
     */
    public SwitchingCodec(CodecSelector selector) {
        this.selector = selector;
    }

    /*
     * (non-Javadoc)
     * 
     * @see nl.flotsam.preon.Codec#decode(nl.flotsam.preon.buffer.BitBuffer,
     * nl.flotsam.preon.Resolver, nl.flotsam.preon.Builder)
     */
    public Object decode(BitBuffer buffer, Resolver resolver, Builder builder)
            throws DecodingException {
        Codec<?> codec = selector.select(buffer, resolver);
        return codec.decode(buffer, resolver, builder);
    }

    /*
     * (non-Javadoc)
     * 
     * @see nl.flotsam.preon.Codec#getSize(nl.flotsam.preon.Resolver)
     */
    public int getSize(Resolver resolver) {
        return selector.getSize(resolver);
    }

    /*
     * (non-Javadoc)
     * 
     * @see nl.flotsam.preon.Codec#getCodecDescriptor()
     */
    public CodecDescriptor getCodecDescriptor() {
        return new CodecDescriptor() {

            public String getLabel() {
                StringBuilder result = new StringBuilder();
                result.append(" either ");
                List<Codec<?>> members = new ArrayList<Codec<?>>(selector.getChoices());
                for (int i = 0; i < members.size(); i++) {
                    if (i != 0) {
                        if (i == members.size() - 1) {
                            result.append(" or ");
                        } else {
                            result.append(", ");
                        }
                    }
                    result.append(members.get(i).getCodecDescriptor().getLabel());
                }
                return result.toString();
            }

            public boolean hasFullDescription() {
                return false;
            }

            public <T> Contents<T> putFullDescription(Contents<T> contents) {
                // TODO Auto-generated method stub
                return contents;
            }

            public <T, V extends ParaContents<T>> V putOneLiner(V para) {
                selector.document(para);
                return para;
            }

            public String getSize() {
                StringBuilder builder = new StringBuilder();
                builder.append("It depends.");
                return builder.toString();
            }

            public <T> void writeReference(ParaContents<T> contents) {
                contents.text(" either ");
                List<Codec<?>> members = new ArrayList<Codec<?>>(selector.getChoices());
                for (int i = 0; i < members.size(); i++) {
                    if (i != 0) {
                        if (i == members.size() - 1) {
                            contents.text(" or ");
                        } else {
                            contents.text(", ");
                        }
                    }
                    members.get(i).getCodecDescriptor().writeReference(contents);
                }
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see nl.flotsam.preon.Codec#getTypes()
     */
    public Class<?>[] getTypes() {
        Set<Class<?>> types = new HashSet<Class<?>>();
        for (Codec<?> codec : selector.getChoices()) {
            types.addAll(Arrays.asList(codec.getTypes()));
        }
        return new ArrayList<Class<?>>(types).toArray(new Class[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see nl.flotsam.preon.Codec#getSize()
     */
    public Expression<Integer, Resolver> getSize() {
        Collection<Codec<?>> choices = selector.getChoices();
        if (choices.size() == 0) {
            return null;
        } else if (choices.size() == 1) {
            return choices.iterator().next().getSize();
        } else {
            Integer size = null;
            Expression<Integer, Resolver> sizeExpr = null;
            for (Codec<?> codec : choices) {
                sizeExpr = codec.getSize();
                if (sizeExpr == null) {
                    return null;
                } else if (!sizeExpr.isParameterized()) {
                    if (size == null) {
                        size = sizeExpr.eval(null);
                    } else {
                        if (!size.equals(sizeExpr.eval(null))) {
                            return null;
                        }
                    }
                }
            }
            if (size != null) {
                return Expressions.createInteger(size, Resolver.class);
            } else {
                return null;
            }
        }
    }

    public Class<?> getType() {
        Set<Class<?>> types = new HashSet<Class<?>>();
        for (Codec<?> codec : selector.getChoices()) {
            types.add(codec.getType());
        }
        Class<?>[] result = new Class<?>[0];
        result = new ArrayList<Class<?>>(types).toArray(result);
        return ClassUtils.calculateCommonSuperType(result);
    }

}
