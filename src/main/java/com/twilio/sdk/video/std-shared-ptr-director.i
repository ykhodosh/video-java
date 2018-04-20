%define %director_shared_ptr(Type)
    %typemap(javadirectorin) std::shared_ptr<Type> "new $typemap(jstype, Type)($1,true)";

    %typemap(directorin,descriptor="Lcom/twilio/sdk/video/$typemap(jstype, Type);") std::shared_ptr<Type> %{
        *($&1_type*)&j$1 = new $1_type($1);
    %}

    %typemap(javadirectorout) std::shared_ptr<Type> "$typemap(jstype, Type).getCPtr($javacall)";

    %typemap(directorout) std::shared_ptr<Type> %{
        $&1_type tmp = NULL;
        *($&1_type*)&tmp = *($&1_type*)&$input;
        if (!tmp) {
            SWIG_JavaThrowException(jenv, SWIG_JavaNullPointerException, "Attempt to dereference null $1_type");
            return NULL;
        }
        $result = *tmp;
    %}

    %typemap(directorin,descriptor="Lcom/twilio/sdk/video/$typemap(jstype, Type);") Type * %{
        std::shared_ptr<PointerHolder> tmpholder(new PointerHolder((void *) $1));
        std::shared_ptr<Type> tmp(tmpholder, (Type *) tmpholder->pointer_);
        *(std::shared_ptr<Type>**)&j$1 = &tmp;
    %}
%enddef
