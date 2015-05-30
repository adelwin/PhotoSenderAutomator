/*
 * File Name       : App.java
 * Class Name      : com.si.diamond.tools.photoSender.App
 * Module Name     : photo-sender-automator
 * Project Name    : PhotoSenderAutomator
 * Author          : adelwin
 * Created Date    : 2015-05-23 15:16:35
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

package com.si.diamond.tools.photoSender;

import com.pru.pacs.base.exception.BaseException;
import com.pru.pacs.base.exception.BaseRuntimeException;
import com.pru.pacs.base.factory.SpringBeanFactory;
import com.pru.pacs.base.util.ExceptionUtil;
import com.si.diamond.tools.photoSender.service.IPhotoUploadService;
import org.apache.log4j.Logger;

public class App {
    protected static Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("starting application");
        try {
            logger.debug("starting spring application context");
            SpringBeanFactory.getInstance();
            SpringBeanFactory.getInstance().registerShutDownHook();
            logger.debug("spring application context started");

            logger.debug("starting upload");
            IPhotoUploadService photoUploadService = (IPhotoUploadService) SpringBeanFactory.getInstance().getBean(IPhotoUploadService.class.getSimpleName());
            photoUploadService.uploadPhotos();
            logger.debug("completed upload");

            logger.debug("shutting down");
            SpringBeanFactory.getInstance().destroy();
            logger.info("application ends");
        } catch (BaseRuntimeException e) {
            logger.error("failed starting spring application context");
            logger.error(ExceptionUtil.getStackTraces(e));
            System.exit(1);
        } catch (BaseException e) {
            logger.error("failed destroying spring application context");
            logger.error(ExceptionUtil.getStackTraces(e));
            System.exit(1);
        }

    }
}
