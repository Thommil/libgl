#include "MatrixUtils.h"
#include "NodeUtils.h"
#include <android/log.h> //__android_log_write(ANDROID_LOG_DEBUG,TAG,MSG);

#include <stdlib.h>

//Storage of static variables
static jclass nodeClass;
static jfieldID modelID;
static jfieldID nodeInstancesID;

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
	JNIEnv *env;
	vm->GetEnv((void**)&env, JNI_VERSION_1_4);
	nodeClass = env->FindClass("fr/kesk/libgl/GlAssets$Node");
	modelID = env->GetFieldID(nodeClass, "model", "[F");
	nodeInstancesID = env->GetFieldID(nodeClass, "nodeInstances", "[Lfr/kesk/libgl/GlAssets$Node;");

	return JNI_VERSION_1_4;
}
