/*
 *	Copyright 2021 Cufy
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *	    http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 */
package org.jamplate.executable;

import org.jamplate.impl.Jamplate;
import org.jamplate.impl.Meta;
import org.jamplate.impl.model.EnvironmentImpl;
import org.jamplate.impl.model.FileDocument;
import org.jamplate.model.Compilation;
import org.jamplate.model.Document;
import org.jamplate.model.Environment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * The main class of the executable.
 *
 * @author LSafer
 * @version 0.2.3
 * @since 0.2.3 ~2021.06.10
 */
public final class JamplateMain {
	/**
	 * Main classes shall have no instances.
	 *
	 * @throws AssertionError when called.
	 * @since 0.2.3 ~2021.06.10
	 */
	private JamplateMain() {
		throw new AssertionError("No instance for you!");
	}

	/**
	 * The main function.
	 *
	 * @param args the arguments.
	 * @throws NullPointerException if the given {@code args} is null.
	 * @since 0.2.3 ~2021.06.10
	 */
	@Contract(pure = true)
	public static void main(@Nullable String @NotNull [] args) {
		Objects.requireNonNull(args, "args");
		Arguments arguments = new Arguments(args);

		File input = new File(arguments.getInput());
		File output = new File(arguments.getOutput());

		Document[] documents = FileDocument.hierarchy(input);

		Environment environment = new EnvironmentImpl();

		environment.getMeta().put(Meta.MEMORY, arguments.getDefaultMemory());
		environment.getMeta().put(Meta.PROJECT, input);
		environment.getMeta().put(Meta.OUTPUT, output);

		boolean compiled = Jamplate.compile(environment, documents);

		if (!compiled) {
			System.err.println("Compilation Error\n");
			environment.getDiagnostic().flush();
			return;
		}

		Compilation[] jamplates = environment
				.compilationSet()
				.stream()
				.filter(compilation -> compilation.getRootTree().document().toString().endsWith(".jamplate"))
				.toArray(Compilation[]::new);

		boolean executed = Jamplate.execute(environment, jamplates);

		if (!executed) {
			System.err.println("Runtime Error\n");
			environment.getDiagnostic().flush();
			return;
		}

		environment.getDiagnostic().flush();
	}

	/**
	 * A class encapsulating the arguments provided to the {@link JamplateMain}.
	 *
	 * @author LSafer
	 * @version 0.2.3
	 * @since 0.2.3 ~2021.06.10
	 */
	public static class Arguments {
		/**
		 * The default memory map.
		 *
		 * @since 0.2.3 ~2021.06.10
		 */
		@NotNull
		protected Map<String, Object> defaultMemory = new HashMap<>();
		/**
		 * The input path.
		 *
		 * @since 0.2.3 ~2021.06.10
		 */
		@NotNull
		protected String input;
		/**
		 * The output path.
		 *
		 * @since 0.2.3 ~2021.06.10
		 */
		@NotNull
		protected String output = "output";

		/**
		 * Construct a new arguments from the given {@code arguments} array.
		 *
		 * @param arguments the arguments array.
		 * @throws NullPointerException if the given {@code arguments} is null.
		 * @since 0.2.3 ~2021.06.10
		 */
		public Arguments(@Nullable String @NotNull [] arguments) {
			Objects.requireNonNull(arguments, "arguments");
			Iterator<String> iterator = Arrays.asList(arguments).iterator();

			while (iterator.hasNext()) {
				String input = iterator.next();

				if (input != null) {
					this.input = input;

					while (iterator.hasNext()) {
						String option = iterator.next();

						if (option != null) {
							int spx;

							spx = option.indexOf('=');
							if (spx != -1) {
								this.defaultMemory.put(
										option.substring(0, spx),
										option.substring(spx + 1)
								);
								continue;
							}

							if (option.equals("-o")) {
								while (iterator.hasNext()) {
									String nextNext = iterator.next();

									if (nextNext != null) {
										this.output = nextNext;
										break;
									}
								}
								continue;
							}

							throw new IllegalArgumentException(
									"Unknown option: " + option);
						}
					}

					return;
				}
			}

			throw new IllegalArgumentException("No input specified!");
		}

		/**
		 * Return the default memory map.
		 *
		 * @return the default memory map.
		 * @since 0.2.3 ~2021.06.10
		 */
		@NotNull
		@Contract(pure = true)
		public Map<String, Object> getDefaultMemory() {
			//noinspection AssignmentOrReturnOfFieldWithMutableType
			return this.defaultMemory;
		}

		/**
		 * Return the input path.
		 *
		 * @return the input path.
		 * @since 0.2.3 ~2021.06.10
		 */
		@NotNull
		@Contract(pure = true)
		public String getInput() {
			return this.input;
		}

		/**
		 * Return the output path.
		 *
		 * @return the output path.
		 * @since 0.2.3 ~2021.06.10
		 */
		@NotNull
		@Contract(pure = true)
		public String getOutput() {
			return this.output;
		}

		/**
		 * Set the input path to be the given {@code input}.
		 *
		 * @param input the new input path.
		 * @return this.
		 * @throws NullPointerException if the given {@code input} is null.
		 * @since 0.2.3 ~2021.06.10
		 */
		@NotNull
		@Contract(value = "_->this", mutates = "this")
		public JamplateMain.Arguments setInput(@NotNull String input) {
			Objects.requireNonNull(input, "input");
			this.input = input;
			return this;
		}

		/**
		 * Set the output path to be the given {@code output}.
		 *
		 * @param output the new output path.
		 * @return this.
		 * @throws NullPointerException if the given {@code output} is null.
		 * @since 0.2.3 ~2021.06.10
		 */
		@NotNull
		@Contract(value = "_->this", mutates = "this")
		public JamplateMain.Arguments setOutput(@NotNull String output) {
			Objects.requireNonNull(output, "output");
			this.output = output;
			return this;
		}
	}
}
