/*******************************************************************************
 * logsniffer, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
 *
 * logsniffer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * logsniffer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.logsniffer.web.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * JAR starter class, which extracts required Jetty libs and JettyLauncher to
 * temp folder and launches Jetty from there.
 * 
 * @author mbok
 * 
 */
public class Starter {

	private static final String JETTY_LAUNCHER_CLASS = "com.logsniffer.web.util.JettyLauncher";

	/**
	 * Extracts required Jetty libs and JettyLauncher to temp folder and
	 * launches Jetty from there.
	 * 
	 * @param args
	 *            Program parameters fully delegated to JettyLauncher
	 * @throws Exception
	 *             in case of any errors
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(final String[] args) throws Exception {
		if (!prepareHomeDir()) {
			System.exit(-1);
		}
		System.out.println("Preparing Jetty...");
		ProtectionDomain domain = Starter.class.getProtectionDomain();
		URL warUrl = domain.getCodeSource().getLocation();

		JarFile jarFile = null;
		ArrayList<URL> execLibs = new ArrayList<URL>();
		try {
			jarFile = new JarFile(warUrl.getPath());
			for (String execLib : getExecLibs(jarFile)) {
				execLibs.add(extractExecLib(jarFile, "exec/" + execLib));
			}

			File launcherLib = File
					.createTempFile("logsniffer", "launcher.jar");
			launcherLib.deleteOnExit();
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
					new FileOutputStream(launcherLib)));
			ZipEntry ze1 = new ZipEntry(JETTY_LAUNCHER_CLASS.replace('.', '/')
					+ ".class");
			zos.putNextEntry(ze1);
			copy(jarFile.getInputStream(jarFile.getEntry("WEB-INF/classes/"
					+ JETTY_LAUNCHER_CLASS.replace('.', '/') + ".class")), zos);

			zos.close();
			execLibs.add(launcherLib.toURI().toURL());
		} finally {
			if (jarFile != null) {
				jarFile.close();
			}
		}

		ClassLoader urlClassLoader = new URLClassLoader(
				execLibs.toArray(new URL[execLibs.size()]));
		Thread.currentThread().setContextClassLoader(urlClassLoader);

		System.out.println("Launching Jetty...");
		Class jettyLauncher = urlClassLoader.loadClass(JETTY_LAUNCHER_CLASS);
		Method mainMethod = jettyLauncher.getMethod("start", new Class[] {
				String[].class, URL.class });
		mainMethod.invoke(jettyLauncher.newInstance(), new Object[] { args,
				warUrl });
		System.out.println("Jetty stopped");
	}

	private static boolean prepareHomeDir() throws Exception {
		if (System.getProperty("logsniffer.home") == null) {
			System.setProperty("logsniffer.home",
					System.getProperty("user.home") + "/logsniffer");
		}
		String logSnifferHomeDir = System.getProperty("logsniffer.home");
		File logSnifferHomeDirFile = new File(logSnifferHomeDir);
		System.out.println("Starting Logsniffer with home directory: "
				+ logSnifferHomeDirFile.getPath());
		if (!logSnifferHomeDirFile.exists()) {
			System.out
					.println("Home directory isn't present, going to create it");
			String errMsg = "Failed to create home directory \""
					+ logSnifferHomeDirFile.getPath()
					+ "\". Logsniffer can't operate without a write enabled home directory. Please create the home directory manually and grant the user Logsniffer is running as the write access.";
			try {
				if (logSnifferHomeDirFile.mkdirs()) {
					return true;
				}
				System.err.println(errMsg);
			} catch (Exception e) {
				System.err.println(errMsg);
				throw e;
			}
		} else if (!logSnifferHomeDirFile.canWrite()) {
			System.err
					.println("Configured home directory \""
							+ logSnifferHomeDirFile.getPath()
							+ "\" isn't write enabled. Logsniffer can't operate without a write enabled home directory. Please grant the user Logsniffer is running as the write access.");
		} else {
			return true;
		}
		return false;
	}

	private static String[] getExecLibs(final JarFile jarFile)
			throws IOException {
		InputStream libsis = null;
		try {
			libsis = jarFile.getInputStream(jarFile.getEntry("exec/libs.txt"));
			return new BufferedReader(new InputStreamReader(libsis)).readLine()
					.split(":");
		} finally {
			if (libsis != null) {
				libsis.close();
			}
		}

	}

	private static URL extractExecLib(final JarFile jarFile,
			final String libPath) throws Exception {
		File tempLib = File.createTempFile("logsniffer",
				libPath.replace("/", "-"));
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(tempLib));
		copy(jarFile.getInputStream(jarFile.getEntry(libPath)), out);
		out.close();
		tempLib.deleteOnExit();
		return tempLib.toURI().toURL();
	}

	private static void copy(final InputStream in, final OutputStream out)
			throws IOException {
		byte[] buffer = new byte[4096 * 32];
		int len;
		while ((len = in.read(buffer)) >= 0) {
			if (len > 0) {
				out.write(buffer, 0, len);
			}
		}
	}

}
