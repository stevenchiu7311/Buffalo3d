#LOCAL_PATH:= $(call my-dir)
#include $(CLEAR_VARS)

#LOCAL_MODULE_TAGS := eng debug optional
#LOCAL_MODULE := Engine3D
#LOCAL_SRC_FILES := $(call all-subdir-java-files)
#include $(BUILD_JAVA_LIBRARY)

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
        $(call all-java-files-under, ../src/min3d) \
        $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := Engine3DSample

include $(BUILD_PACKAGE)

