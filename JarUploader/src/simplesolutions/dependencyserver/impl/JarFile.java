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
public final class JarFile implements XMLParseable {

	/**
	 * The Class PackageVersion.
	 */
	public final static class PackageVersionRange {

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
		public PackageVersionRange(String packageDescriptor) {
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
		 * @return the string with the version as it is in the manifest.
		 */
		private String getPackageVersion(String packageNameEntry) {
			String[] tmp1 = packageNameEntry.split("version=\"");
			if (tmp1.length != 2)
				return null; // No version declared
			String[] version = tmp1[1].split("\""); // split over the last
													// quotation
													// mark.
			return version[0];
		}

		/**
		 * Checks if the version range is compatible.
		 * <p>
		 * <b>Note:</b> One of the PackageVersionRange being compared must have
		 * a concrete version instead of a range of versions! Otherwise no
		 * comparison can be made.<br>
		 * i.e. The exported packages provide a concrete version while imported
		 * packages support a given range of versions. You can only compare a
		 * version with a version range, not two versions ranges. Therefore you
		 * can only compare a PackageVersionRange from an exported package with
		 * the PackageVersionRange of an imported package.
		 * 
		 * @param other
		 *            the other version range to compare.
		 * @return true, if is compatible
		 */
		public boolean isCompatible(PackageVersionRange other) {
			if (this.isVersionRange() && other.isVersionRange())
				return false; // Cannot compare two ranges!
			if (this.isVersionRange()) {
				boolean minTest = false;
				boolean maxTest = false;
				/*
				 * Compare with lower bound.
				 */
				switch (minVersionComparisonMethod) {
				case EQUAL:
					if (other.getMinVersion().compareTo(minVersion) == 0)
						minTest = true;
					break;
				case EQUAL_OR_GREATER:
					if (other.getMinVersion().compareTo(minVersion) >= 0)
						minTest = true;
					break;
				case GREATER:
					if (other.getMinVersion().compareTo(minVersion) > 0)
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
					if (other.getMinVersion().compareTo(maxVersion) == 0)
						maxTest = true;
					break;
				case EQUAL_OR_LOWER:
					if (other.getMinVersion().compareTo(maxVersion) <= 0)
						maxTest = true;
					break;
				case LOWER:
					if (other.getMinVersion().compareTo(maxVersion) < 0)
						maxTest = true;
					break;
				default:
					break;
				}
				return minTest && maxTest;
			} else
				return other.isCompatible(this);
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
	private final Map<String, PackageVersionRange> exportedPackages;

	/** The imported packages. */
	private final Map<String, PackageVersionRange> importedPackages;

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
	public JarFile(String name, String[] importedPackages,
			String[] exportedPackages) {
		this.name = name;
		this.exportedPackages = new HashMap<String, PackageVersionRange>();
		this.importedPackages = new HashMap<String, PackageVersionRange>();
		for (String p : importedPackages)
			this.importedPackages.put(p, new PackageVersionRange(p));
		for (String p : exportedPackages)
			this.exportedPackages.put(p, new PackageVersionRange(p));
	}

	@Override
	public boolean fromXML() {
		//TODO:
		return false;
	}

	/**
	 * Gets the exported packages.
	 * 
	 * @return the exported packages
	 */
	public ImmutableMap<String, PackageVersionRange> getExportedPackages() {
		return new ImmutableMap<String, PackageVersionRange>(exportedPackages);
	}

	/**
	 * Gets the imported packages.
	 * 
	 * @return the imported packages
	 */
	public ImmutableMap<String, PackageVersionRange> getImportedPackages() {
		return new ImmutableMap<String, PackageVersionRange>(importedPackages);
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
		sb.append("<jar name=\"" + this.name + "\">\n");
		/*
		 * Print exported packages
		 */
		sb.append("\t<exported>\n");
		for (Map.Entry<String, PackageVersionRange> entry : exportedPackages
				.entrySet())
			sb.append("\t\t<package name=\"" + entry.getKey() + "\" version=\""
					+ entry.getValue() + "\">\n");
		sb.append("\t</exported>\n");
		/*
		 * Print imported packages
		 */
		sb.append("\t<imported>\n");
		for (Map.Entry<String, PackageVersionRange> entry : importedPackages
				.entrySet())
			sb.append("\t\t<package name=\"" + entry.getKey() + "\" version=\""
					+ entry.getValue() + "\">\n");
		sb.append("\t</imported>\n");
		sb.append("</jar>\n");
		return sb.toString();
	}
}
