LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := eng debug optional
LOCAL_MODULE := Engine3D
LOCAL_SRC_FILES := $(call all-java-files-under, src)
include $(BUILD_JAVA_LIBRARY)

