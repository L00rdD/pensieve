# Release rules on top of proguard-android-optimize.txt. The app has no reflection,
# no serialisation library and no JNI: nothing to keep by name. The JMGO middleware is
# reached over a Binder by its interface descriptor, not by Java reflection.
-allowaccessmodification
-repackageclasses ''
