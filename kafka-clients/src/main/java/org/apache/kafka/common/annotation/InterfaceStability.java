/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.kafka.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 注释，告知用户在多大程度上依赖于不随时间变化的特定包、类或方法。
 * 目前的稳定性可以是{@link Stable}、{@link Evolving}或 {@link Unstable}。
 * @author 章云
 * @date 2019/9/23 21:37
 */
@InterfaceStability.Evolving
public class InterfaceStability {

    /**
     * 可以在保持对较小版本边界的兼容性的同时进化。
     * 只能在主要版本(即在m.0)。
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Stable {}

    /**
     * 进化，但可能在次要版本(即m.x)上破坏兼容性
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Evolving {}

    /**
     * 对于跨越任何版本粒度级别的可靠性或稳定性，没有提供任何保证。
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Unstable {}

}