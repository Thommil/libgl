LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := LGL
LOCAL_CFLAGS    := -Werror
LOCAL_SRC_FILES := GLES20Utils.cpp MatrixUtils.cpp NodeUtils.cpp
LOCAL_LDLIBS    := -lGLESv2 -llog -lm

include $(BUILD_SHARED_LIBRARY)
