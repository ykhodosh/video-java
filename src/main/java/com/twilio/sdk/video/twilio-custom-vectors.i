// wrap std::vector<std::shared_ptr<>> as java.util.List
%define %shared_ptr_vector_as_immutable_list(CType, JType)

%typemap(jni) std::vector<std::shared_ptr<CType>> "jlongArray"
%typemap(jtype) std::vector<std::shared_ptr<CType>> "long[]"
%typemap(jstype) std::vector<std::shared_ptr<CType>> "java.util.List<JType>"

%typemap(javain,
         pre="    long[] temp$javainput = new long[$javainput.size()];
    for (int index = 0; index < $javainput.size(); index++)
        temp$javainput[index] = JType.getCPtr($javainput.get(index));",
         pgcppname="temp$javainput") std::vector<std::shared_ptr<CType>> "temp$javainput"

%typemap(javaout) std::vector<std::shared_ptr<CType>> {
    final java.util.List<JType> jtype_list = new java.util.ArrayList<>();
    final long[] cptrs = $jnicall;
    if (cptrs != null) {
      for (long cptr: cptrs) {
        jtype_list.add(new JType(cptr, true));
      }
    }
    return java.util.Collections.unmodifiableList(jtype_list);
}

%typemap(in) std::vector<std::shared_ptr<CType>> {
  std::shared_ptr<CType> *ctype;
  std::vector<std::shared_ptr<CType>> tmp;
  $1 = tmp;
  if ($input) {
    jint size = JCALL1(GetArrayLength, jenv, $input);
    jboolean is_copy = JNI_FALSE;
    jlong *elements = (jlong *) JCALL2(GetPrimitiveArrayCritical, jenv, $input, &is_copy);
    for (int i = 0; i < size; i++) {
      *(std::shared_ptr<CType> **) &ctype = (std::shared_ptr<CType> *) elements[i];
      $1.push_back(*ctype);
    }
    JCALL3(ReleasePrimitiveArrayCritical, jenv, $input, (void *) elements, JNI_ABORT);
  }
}

%typemap(out) std::vector<std::shared_ptr<CType>> {
  if ($1.size() > 0) {
    $result = JCALL1(NewLongArray, jenv, $1.size());
    jlong *cptrs = new jlong[$1.size()];
    for (int i = 0; i < $1.size(); i++) {
      std::shared_ptr<CType> ctype = $1.at(i);
      cptrs[i] = (jlong) new std::shared_ptr<CType>(ctype);
      JCALL4(SetLongArrayRegion, jenv, $result, 0, $1.size(), cptrs);
    }
    delete[] cptrs;
  }
}
%enddef

// wrap std::vector<std::string> as java.util.List<String>
%typemap(jni) std::vector<std::string> "jobjectArray"
%typemap(jtype) std::vector<std::string> "String[]"
%typemap(jstype) std::vector<std::string> "java.util.List<String>"

%typemap(javain,
         pre="    String[] temp$javainput = $javainput.toArray(new String[$javainput.size()]);",
         pgcppname="temp$javainput") std::vector<std::string> "temp$javainput"

%typemap(javaout) std::vector<std::string> {
    final String[] tempresult = $jnicall;
    final java.util.List<String> result;
    if (tempresult == null)
      result = java.util.Collections.unmodifiableList(new java.util.ArrayList<>());
    else
      result = java.util.Collections.unmodifiableList(java.util.Arrays.asList(tempresult));
    return result;
}

%typemap(in) std::vector<std::string> {
  if ($input) {
    jint size = JCALL1(GetArrayLength, jenv, $input);
    for (int i = 0; i < size; i++) {
      jstring j_string = (jstring) JCALL2(GetObjectArrayElement, jenv, $input, i);
      const char *c_string = JCALL2(GetStringUTFChars, jenv, j_string, 0);
      $1.push_back(c_string);
      JCALL2(ReleaseStringUTFChars, jenv, j_string, c_string);
      JCALL1(DeleteLocalRef, jenv, j_string);
    }
  }
}

%typemap(out) std::vector<std::string> {
  if ($1.size() > 0) {
    const jclass clazz = JCALL1(FindClass, jenv, "java/lang/String");
    $result = JCALL3(NewObjectArray, jenv, $1.size(), clazz, NULL);
    for (int i = 0; i < $1.size(); i++) {
      jstring temp_string = JCALL1(NewStringUTF, jenv, $1.at(i).c_str());
      JCALL3(SetObjectArrayElement, jenv, $result, i, temp_string);
      JCALL1(DeleteLocalRef, jenv, temp_string);
    }
  }
}
