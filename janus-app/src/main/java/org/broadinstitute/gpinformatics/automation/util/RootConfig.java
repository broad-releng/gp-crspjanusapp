package org.broadinstitute.gpinformatics.automation.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Creates directories for robo files and messages.
 * Allows for moving and copying of files
 */
public class RootConfig {
    private static final Logger gLog = LoggerFactory.getLogger(RootConfig.class);
    private static final String DEFAULT_DIR = "C:\\MESSAGING";
    private static final String ROBO_DIR = "ROBO";
    private static final String ROBO_DIR_ARCHIVE = "ROBO_ARCHIVE";
    private static final String STASH_DIR = "stash";
    private static final String MESSAGE_ARCHIVE = "transferred";

    private File mRootDirectory;
    private File mRoboDirectory;
    private File mRoboDirectoryArchive;
    private File mStashDir;
    private File mMessageArchiveDir;

    public RootConfig() {
        this(DEFAULT_DIR);
    }

    public RootConfig(String rootDir) {
        this(new File(rootDir));
    }

    public RootConfig(File rootDir) {
        setRootDirectory(rootDir);
    }

    public void setRootDirectory(File rootDirectory) {
        gLog.info("RootConfig: Setting root directory to " + rootDirectory.getAbsolutePath());
        mRootDirectory = rootDirectory;
        mRoboDirectory = new File(mRootDirectory,ROBO_DIR);
        mRoboDirectoryArchive = new File(mRootDirectory, ROBO_DIR_ARCHIVE);
        mStashDir = new File(mRootDirectory, STASH_DIR);
        mMessageArchiveDir = new File(mRootDirectory, MESSAGE_ARCHIVE);

        createDirectory(mRootDirectory);
        createDirectory(mRoboDirectory);
        createDirectory(mRoboDirectoryArchive);
        createDirectory(mMessageArchiveDir);
    }

    public File getRoboDirectory() {
        createDirectory(mRoboDirectory);
        return mRoboDirectory;
    }

    private void createDirectory(File file) {
        if(file != null && !file.exists())
            file.mkdir();
    }

    public void setRoboDirectory(String roboDir) {
        mRoboDirectory = new File(mRootDirectory, roboDir);
        if(!mRoboDirectory.exists()){
            mRoboDirectory.mkdir();
        }
    }

    public File getRoboDirectoryArchive() {
        return mRoboDirectoryArchive;
    }

    public File getStashDir() {
        return mStashDir;
    }

    public File getMessageArchiveDir() {
        return mMessageArchiveDir;
    }

    public void moveRoboDirectoryContentsToArchive() throws IOException {
        for(File file:  mRoboDirectory.listFiles((FilenameFilter) new SuffixFileFilter(".csv"))){
            FileUtils.moveFileToDirectory(file,mRoboDirectoryArchive,true);
        }
    }

    public File getRootDirectory() {
        return mRootDirectory;
    }
}
