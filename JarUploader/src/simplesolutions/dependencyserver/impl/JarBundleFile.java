package simplesolutions.dependencyserver.impl;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Version;

import simplesolutions.dependencyserver.impl.XMLDataBase.XMLParseable;
import simplesolutions.util.ImmutableMap;

/**
 * The Class representing each registered jar file.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 */
public final class JarBundleFile implements XMLParseable {

	/**
	 * The Class PackageVersion.
	 */
	public final static class PackageVersion {

		/**
		 * The Enum ComparisonMethod.
		 */
		public enum ComparisonMethod {

			/** The equal. */
			EQUAL,
			/** The equal or greater. */
			EQUAL_OR_GREATER,
			/** The equal or lower. */
			EQUAL_OR_LOWER,
			/** The greater. */
			GREATER,
			/** The lower. */
			LOWER
		}

		/** The max version. */
		private final Version maxVersion;

		/** The max version comparison method. */
		private final ComparisonMethod maxVersionComparisonMethod;

		/** The min version. */
		private final Version minVersion;

		/** The min version comparison method. */
		private final ComparisonMethod minVersionComparisonMethod;

		/** The version string. */
		private final String versionString;

		/**
		 * Instantiates a new package version.
		 * 
		 * @param packageDescriptor
		 *            the package descriptor as it is in a manifest file. Eg.:
		 *            <code>javax.wsdl.xml;version="1.5"</code>.
		 */
		public PackageVersion(String packageDescriptor) {
			String versionRange = getPackageVersion(packageDescriptor);
			this.versionString = versionRange;
			if (versionRange.contains(",")) { // Is a version range?
				if (versionRange.charAt(0) == '(')
					minVersionComparisonMethod = ComparisonMethod.GREATER;
				else
					minVersionComparisonMethod = ComparisonMethod.EQUAL_OR_GREATER;
				if (versionRange.charAt(versionRange.length() - 1) == ')')
					maxVersionComparisonMethod = ComparisonMethod.LOWER;
				else
					maxVersionComparisonMethod = ComparisonMethod.EQUAL_OR_LOWER;
				/*
				 * Remove brackets.
				 */
				versionRange = versionRange.substring(0,
						versionRange.length() - 1);
				versionRange = versionRange.substring(1, versionRange.length());
				String[] minMaxVersions = versionRange.split(",");
				minVersion = new Version(minMaxVersions[0]);
				maxVersion = new Version(minMaxVersions[1]);
			} else {
				minVersion = maxVersion = new Version(versionRange);
				minVersionComparisonMethod = maxVersionComparisonMethod = ComparisonMethod.EQUAL;
			}
		}

		/**
		 * Gets the max version.
		 * 
		 * @return the max version
		 */
		public Version getMaxVersion() {
			return maxVersion;
		}

		/**
		 * Gets the max version comparison method.
		 * 
		 * @return the max version comparison method
		 */
		public ComparisonMethod getMaxVersionComparisonMethod() {
			return maxVersionComparisonMethod;
		}

		/**
		 * Gets the min version.
		 * 
		 * @return the min version
		 */
		public Version getMinVersion() {
			return minVersion;
		}

		/**
		 * Gets the concrete version, assuming this is not a version range.
		 * 
		 * @return the concrete version
		 */
		public Version getConcreteVersion() {
			if (this.isVersionRange() == true)
				throw new IllegalAccessError(
						"This package version is not a concrete version!");
			return getMinVersion();
		}

		/**
		 * Gets the min version comparison method.
		 * 
		 * @return the min version comparison method
		 */
		public ComparisonMethod getMinVersionComparisonMethod() {
			return minVersionComparisonMethod;
		}

		/**
		 * Parses the package version.
		 * 
		 * @param packageNameEntry
		 *            the package name entry.
		 * @return the string with the version as it is in the manifest, or
		 *         "0.0.0" if no version is declared.
		 */
		private String getPackageVersion(String packageNameEntry) {
			String[] tmp1 = packageNameEntry.split("version=\"");
			if (tmp1.length != 2)
				return "0.0.0"; // No version declared
			String[] version = tmp1[1].split("\""); // split over the last
													// quotation
													// mark.
			return version[0];
		}

		/**
		 * Checks if the version range is compatible.
		 * <p>
		 * <b>Note:</b> One of the PackageVersion being compared must have
		 * a concrete version instead of a range of versions! Otherwise no
		 * comparison can be made.<br>
		 * i.e. The exported packages provide a concrete version while imported
		 * packages support a given range of versions. You can only compare a
		 * version with a version range, not two versions ranges. Therefore you
		 * can only compare a PackageVersion from an exported package with
		 * the PackageVersion of an imported package.
		 * 
		 * @param other
		 *            the other version range to compare.
		 * @return true, if is compatible
		 */
		public boolean isCompatible(PackageVersion other) {
			if (this.isVersionRange() && other.isVersionRange())
				return false; // Cannot compare two ranges!
			if (this.isVersionRange() || !other.isVersionRange()) {
				boolean minTest = false;
				boolean maxTest = false;
				/*
				 * If this version is 0.0.0 we don't care about versions, any
				 * version will be compatible.
				 */
				if (this.getMinVersion().compareTo(new Version(0, 0, 0)) == 0)
					return true;
				/*
				 * Compare with lower bound.
				 */
				switch (minVersionComparisonMethod) {
				case EQUAL:
					if (other.getConcreteVersion().compareTo(minVersion) == 0)
						minTest = true;
					break;
				case EQUAL_OR_GREATER:
					if (other.getConcreteVersion().compareTo(minVersion) >= 0)
						minTest = true;
					break;
				case GREATER:
					if (other.getConcreteVersion().compareTo(minVersion) > 0)
						minTest = true;
					break;
				default:
					break;
				}
				/*
				 * Compare with upper bound.
				 */
				switch (maxVersionComparisonMethod) {
				case EQUAL:
					if (other.getConcreteVersion().compareTo(maxVersion) == 0)
						maxTest = true;
					break;
				case EQUAL_OR_LOWER:
					if (other.getConcreteVersion().compareTo(maxVersion) <= 0)
						maxTest = true;
					break;
				case LOWER:
					if (other.getConcreteVersion().compareTo(maxVersion) < 0)
						maxTest = true;
					break;
				default:
					break;
				}
				return minTest && maxTest;
			} else {
				return other.isCompatible(this);
			}
		}

		/**
		 * Checks if this instance represents a concrete version or a version
		 * range.
		 * 
		 * @return true, if is version range
		 */
		public boolean isVersionRange() {
			return !(this.minVersionComparisonMethod
					.equals(ComparisonMethod.EQUAL) && this.maxVersionComparisonMethod
					.equals(ComparisonMethod.EQUAL));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return versionString;
		}
	}

	/** The exported packages. */
	private final Map<String, PackageVersion> exportedPackages;

	/** The imported packages. */
	private final Map<String, PackageVersion> importedPackages;

	/** The name. */
	private final String name;

	/**
	 * Instantiates a new jar file.
	 * 
	 * @param name
	 *            the name
	 * @param importedPackages
	 *            the imported packages
	 * @param exportedPackages
	 *            the exported packages
	 */
	public JarBundleFile(String name, String[] importedPackages,
			String[] exportedPackages) {
		this.name = name;
		this.exportedPackages = new HashMap<String, PackageVersion>();
		this.importedPackages = new HashMap<String, PackageVersion>();
		if (importedPackages != null)
			for (String p : importedPackages)
				this.importedPackages.put(p.split(";")[0],
						new PackageVersion(p));
		if (exportedPackages != null)
			for (String p : exportedPackages)
				this.exportedPackages.put(p.split(";")[0],
						new PackageVersion(p));
	}

	@Override
	public boolean fromXML() {
		// TODO:
		return false;
	}

	/**
	 * Gets the exported packages.
	 * 
	 * @return the exported packages
	 */
	public ImmutableMap<String, PackageVersion> getExportedPackages() {
		return new ImmutableMap<String, PackageVersion>(exportedPackages);
	}

	/**
	 * Gets the imported packages.
	 * 
	 * @return the imported packages
	 */
	public ImmutableMap<String, PackageVersion> getImportedPackages() {
		return new ImmutableMap<String, PackageVersion>(importedPackages);
	}

	/**
	 * Seeks for a given package with a given version inside this jar file
	 * (bundle).
	 * 
	 * @param packageNameManifest
	 *            the package name in the OSGi manifest.mf format, ex.:
	 *            <i>foo.bar.lol;version="1.2.3"</i> or just <i>foo.bar.lol</i>
	 * @return true, if successful
	 */
	public boolean providesPackage(String packageNameManifest) {
		String packageName = packageNameManifest.split(";")[0];
		PackageVersion version = new PackageVersion(
				packageNameManifest);
		for (Map.Entry<String, PackageVersion> e : this
				.getExportedPackages().entrySet())
			if (e.getKey().equals(packageName)
					&& version.isCompatible(e.getValue()))
				return true;
		return false;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<jar name=\"" + this.name + "\">\r\n");
		/*
		 * Print exported packages
		 */
		sb.append("\t<exported>\r\n");
		for (Map.Entry<String, PackageVersion> entry : exportedPackages
				.entrySet())
			sb.append("\t\t<package name=\"" + entry.getKey() + "\" version=\""
					+ entry.getValue() + "\" />\r\n");
		sb.append("\t</exported>\r\n");
		/*
		 * Print imported packages
		 */
		sb.append("\t<imported>\r\n");
		for (Map.Entry<String, PackageVersion> entry : importedPackages
				.entrySet())
			sb.append("\t\t<package name=\"" + entry.getKey() + "\" version=\""
					+ entry.getValue() + "\" />\r\n");
		sb.append("\t</imported>\n");
		sb.append("</jar>");
		return sb.toString();
	}
}
