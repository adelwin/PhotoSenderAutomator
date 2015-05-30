/*
 * File Name       : PhotoUploadServiceImpl.java
 * Class Name      : com.si.diamond.tools.photoSender.service.PhotoUploadServiceImpl
 * Module Name     : photo-sender-automator
 * Project Name    : PhotoSenderAutomator
 * Author          : adelwin
 * Created Date    : 2015-05-23 17:01:45
 *
 * Copyright (c) 2015 Solveware Independent. All Rights Reserved.
 * This software contains confidential and proprietary information of Solveware Independent.
 *
 * |=================|==================|=========|======================================
 * | Author          | Date             | Version | Description
 * |=================|==================|=========|======================================
 * |                 |                  |         |
 * |                 |                  |         |
 * |=================|==================|=========|======================================
 */

package com.si.diamond.tools.photoSender.service.impl;

import com.pru.pacs.base.exception.BaseException;
import com.pru.pacs.base.file.filter.BaseFileNameFilter;
import com.pru.pacs.base.file.finder.BaseFinder;
import com.pru.pacs.base.mail.exception.BaseMailException;
import com.pru.pacs.base.mail.util.MailUtil;
import com.pru.pacs.base.service.impl.BaseServiceImpl;
import com.pru.pacs.base.util.DateUtil;
import com.pru.pacs.base.util.ExceptionUtil;
import com.pru.pacs.base.util.FileUtil;
import com.si.diamond.tools.photoSender.service.IPhotoUploadService;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by adelwin on 23/05/2015.
 */
public class PhotoUploadServiceImpl extends BaseServiceImpl implements IPhotoUploadService {
    protected Logger logger = Logger.getLogger(PhotoUploadServiceImpl.class);
    private String baseLocation;
    private BaseFileNameFilter fileNameFilter;
    private BaseFinder finder;
    private MailUtil mailUtil;

    public String getBaseLocation() {
        return baseLocation;
    }

    public void setBaseLocation(String baseLocation) {
        this.baseLocation = baseLocation;
    }

    public BaseFinder getFinder() {
        return finder;
    }

    public void setFinder(BaseFinder finder) {
        this.finder = finder;
    }

    public BaseFileNameFilter getFileNameFilter() {
        return fileNameFilter;
    }

    public void setFileNameFilter(BaseFileNameFilter fileNameFilter) {
        this.fileNameFilter = fileNameFilter;
    }

    public MailUtil getMailUtil() {
        return mailUtil;
    }

    public void setMailUtil(MailUtil mailUtil) {
        this.mailUtil = mailUtil;
    }

    @Override
    public void uploadPhotos() {
        logger.debug("starting upload photos");
        logger.debug("finding files");
        List<File> inputFiles = this.getFinder().findFiles(this.getBaseLocation(), this.getFileNameFilter());
        logger.debug("found [" + inputFiles.size() + "] files");

        logger.debug("iterating files and sending");
        for (File currentFile : inputFiles) {
            boolean operationResult = false;
            logger.debug("currently on file [" + currentFile.getAbsolutePath() + "]");
            logger.debug("marking interim file");
            String interimName = this.moveToInterimFile(currentFile);
            if (interimName == null) {
                logger.debug("failed marking interim file");
                logger.debug("moving to failed");
                this.moveToFailed(currentFile);
            }
            logger.debug("interim file is [" + interimName + "]");
            File interimFile = new File(interimName);

            try {
                logger.debug("sending photo");
                this.getMailUtil().sendMail(MailUtil.Level.INFO, "Photo " + DateUtil.getDateAsString(DateUtil.getTodayDate(), "yyyy-MM-dd"), "Photo " + DateUtil.getDateAsString(DateUtil.getTodayDate(), "yyyy-MM-dd"), interimFile);
                logger.debug("photo sent");
                operationResult = true;
            } catch (BaseMailException e) {
                logger.error("error sending photo");
                logger.error(ExceptionUtil.getStackTraces(e));
                operationResult = false;
            }

            if (operationResult == true) {
                this.moveToSuccess(interimFile);
            } else {
                this.moveToFailed(interimFile);
            }
        }
        logger.debug("finished iterating");
    }

    private String moveToInterimFile(File file) {
        logger.debug("getting last mod date");
        Date lastModifiedDate = new Date(file.lastModified());
        String dateString = DateUtil.getDateAsString(lastModifiedDate, "yyyy_MM_dd");

        String pathPart = FilenameUtils.getFullPath(file.getAbsolutePath());
        String namePart = FilenameUtils.getBaseName(file.getName());
        String extPart = FilenameUtils.getExtension(file.getName());

        String interimName = pathPart + namePart + "_" + dateString + "." + extPart;
        logger.debug("interim file name = [" + interimName + "]");

        boolean result = file.renameTo(new File(interimName));
        if (result == true) {
            return interimName;
        } else {
            return null;
        }
    }

    private boolean moveToFailed(File file) {
        logger.debug("splitting file names");
        String pathPart = FilenameUtils.getFullPath(file.getAbsolutePath());
        String namePart = FilenameUtils.getBaseName(file.getName());
        String extPart = FilenameUtils.getExtension(file.getName());

        String failName = pathPart + "fail/" + namePart + "." + extPart;
        logger.debug("failed file name = [" + failName + "]");

        boolean result = file.renameTo(new File(failName));
        return result;
    }

    private boolean moveToSuccess(File file) {
        logger.debug("splitting file names");
        String pathPart = FilenameUtils.getFullPath(file.getAbsolutePath());
        String namePart = FilenameUtils.getBaseName(file.getName());
        String extPart = FilenameUtils.getExtension(file.getName());

        String successName = pathPart + "success/" + namePart + "." + extPart;
        logger.debug("success file name = [" + successName + "]");

        boolean result = file.renameTo(new File(successName));
        return result;
    }

}
