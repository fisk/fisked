/*******************************************************************************
 * Copyright (c) 2017, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.fisked.buffer.basic.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackages;

import java.io.IOException;

import javax.inject.Inject;

import org.fisked.IApplication;
import org.fisked.behavior.RootBehaviorProvider;
import org.fisked.buffer.Buffer;
import org.fisked.buffer.basic.utilities.AppUtilities;
import org.fisked.buffer.basic.utilities.TestEventLoop;
import org.fisked.launcher.TestLauncherService;
import org.fisked.mode.AbstractMode;
import org.fisked.mode.NormalMode;
import org.fisked.renderingengine.service.IConsoleService;
import org.fisked.renderingengine.service.ICursorService;
import org.fisked.ui.buffer.BufferWindow;
import org.fisked.util.models.AttributedString;
import org.fisked.util.models.Color;
import org.fisked.util.models.Range;
import org.fisked.util.models.Rectangle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class EditBufferTextTest {
	private final static Logger LOG = LoggerFactory.getLogger(EditBufferTextTest.class);

	@Inject
	private BundleContext _bundleContext;

	@Inject
	private IApplication _application;

	@Before
	public void bootstrap() {
		LOG.trace("Starting application.");
		_application.start(new TestLauncherService());
		RootBehaviorProvider.getInstance().setCallerBehavior(AbstractMode.class, IConsoleService.class,
				new IConsoleService() {

					@Override
					public void activate() {
					}

					@Override
					public void deactivate() {
					}

					@Override
					public int getChar() throws IOException {
						return 0;
					}

					@Override
					public void flush() {
					}

					@Override
					public int getScreenWidth() {
						return 1024;
					}

					@Override
					public int getScreenHeight() {
						return 1024;
					}

					@Override
					public IRenderingContext getRenderingContext() {
						return new IRenderingContext() {

							@Override
							public void moveTo(int x, int y) {
							}

							@Override
							public void printString(String string) {
							}

							@Override
							public void printString(AttributedString string) {
							}

							@Override
							public void clearScreen() {
							}

							@Override
							public void clearScreen(Color color) {
							}

							@Override
							public void clearRect(Rectangle rect, Color color) {
							}

							@Override
							public void close() {
							}

						};
					}

					@Override
					public ICursorService getCursorService() {
						return cursor -> {
						};
					}

					@Override
					public void scrollTextRegionUp(Range range) {
					}

					@Override
					public void scrollTextRegionDown(Range range) {
					}

				});
	}

	@After
	public void tearDown() {
		LOG.trace("Stopping application.");
		_application.exit(-1);
	}

	@Configuration
	public Option[] config() {
		return options(systemPackages("org.fisked.launcher.service"),
				mavenBundle("org.apache.felix", "org.apache.felix.ipojo").versionAsInProject(),
				mavenBundle("org.osgi", "org.osgi.core").versionAsInProject(),
				mavenBundle("javax.mail", "mail").versionAsInProject(),
				mavenBundle("org.ops4j.pax.logging", "pax-logging-api").versionAsInProject(),
				mavenBundle("fisked", "fisked-util").versionAsInProject(),
				mavenBundle("fisked", "fisked-behavior").versionAsInProject(),
				mavenBundle("fisked", "fisked-java").versionAsInProject(),
				mavenBundle("fisked", "fisked-email").versionAsInProject(),
				mavenBundle("fisked", "fisked-email-imap").versionAsInProject(),
				mavenBundle("fisked", "fisked-email-smtp").versionAsInProject(), junitBundles());
	}

	@Test
	public void shouldHaveInjectedTheBundleContextOfThisTestBundle() {
		assertNotNull(_bundleContext);
		assertTrue("JUnit Test Bundle should start with PAX",
				_bundleContext.getBundle().getSymbolicName().startsWith("PAX"));
	}

	@Test
	public void testWriteHello() {
		AppUtilities utilities = new AppUtilities();
		BufferWindow window = utilities.getWindow();
		TestEventLoop loop = utilities.getEventLoop(window);
		Buffer buffer = window.getBuffer();

		loop.feedEvent("ihello world!\u001B");
		String result = buffer.toString();

		LOG.debug("Result: " + result);
		Assert.assertTrue(window.getCurrentMode() instanceof NormalMode);
		Assert.assertEquals(result, "hello world!");
	}
}
