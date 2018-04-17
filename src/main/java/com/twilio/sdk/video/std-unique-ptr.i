namespace std {
    %feature("novaluewrapper") unique_ptr;
    template <typename Type> struct unique_ptr {
        typedef Type *pointer;

        constexpr unique_ptr();
        explicit unique_ptr(pointer Ptr);
        unique_ptr(unique_ptr &&Right);
        template<class Type2, Class Del2> unique_ptr(unique_ptr<Type2, Del2> &&Right);
        unique_ptr(const unique_ptr &Right) = delete;

        pointer operator->() const;
        pointer release();
        void reset(pointer __p=pointer());
        void swap(unique_ptr &__u);
        pointer get() const;
        operator bool() const;

        ~unique_ptr();
    };
}

%define %unique_ptr(Type)
    %typemap(jni) std::unique_ptr<Type> "jlong"
    %typemap(jtype) std::unique_ptr<Type> "long"
    %typemap(jstype) std::unique_ptr<Type> "$typemap(jstype, Type)"

    %typemap(out) std::unique_ptr<Type> %{
        jlong lpp = 0;
        *(Type**) &lpp = new $1_ltype(std::move($1));
        $result = lpp;
    %}

    %typemap(javain) std::unique_ptr<Type>,
                     std::unique_ptr<Type> &,
                     std::unique_ptr<Type> *,
                     std::unique_ptr<Type> *& "$typemap(jstype, Type).getCPtr($javainput)"

    %typemap(javaout) std::unique_ptr<Type> %{
        long cPtr = $jnicall;
        return (cPtr == 0) ? null : new $typemap(jstype, Type)(cPtr, true);
    %}

    %typemap(in) std::unique_ptr<Type> %{
        $&1_type tmp = NULL;
        *($&1_type*)&tmp = *($&1_type*)&$input;
        if (!tmp) {
            SWIG_JavaThrowException(jenv, SWIG_JavaNullPointerException, "Attempt to dereference null $1_type");
            return $null;
        }
        $1.reset(tmp->release());
    %}

    %typemap(javadirectorin) std::unique_ptr<Type> "new $typemap(jstype, Type)($1,true)";
    %typemap(directorin,descriptor="Lcom/twilio/sdk/video/$typemap(jstype, Type);") std::unique_ptr<Type> %{
        *(Type**)&j$1 = $1.release();
    %}

    %typemap(javadirectorout) std::unique_ptr<Type> "$typemap(jstype, Type).getCPtr($javacall)";
    %typemap(directorout) std::unique_ptr<Type> %{
        $&1_type tmp = NULL;
        *($&1_type*)&tmp = *($&1_type*)&$input;
        if (!tmp) {
            SWIG_JavaThrowException(jenv, SWIG_JavaNullPointerException, "Attempt to dereference null $1_type");
            return $null;
        }
        $1.reset(tmp->release());
    %}

    %template() std::unique_ptr<Type>;
    %newobject std::unique_ptr<Type>::release;
%enddef
