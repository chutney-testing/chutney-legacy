/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * New API to manipulate Actions.<br>
 * This API makes
 * <ul>
 *     <li>actions inputs accessible to the execution, so that parameters of all steps used in a scenario can be validated before actual execution</li>
 *     <li>actions inputs accessible to design, so that auto-completion and editor validation can be performed</li>
 *     <li>just-in-time expression evaluation transparent to action implementations</li>
 *     <li>type-safe inputs declaration</li>
 * </ul>
 */
package com.chutneytesting.action;
