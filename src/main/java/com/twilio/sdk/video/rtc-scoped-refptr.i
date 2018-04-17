namespace rtc {
template <class Type> class scoped_refptr {
 public:
  scoped_refptr(Type *ptr);
  scoped_refptr(const scoped_refptr &right);

  Type *get() const;
  Type *operator->() const;

  Type *release();

  void swap(scoped_refptr &other);

  ~scoped_refptr();

  %extend {
    scoped_refptr<Type> &assign(const scoped_refptr<Type> &right) {
      *$self = right;
      return *$self;
    }

    scoped_refptr<Type> &assign(Type *right) {
      *$self = right;
      return *$self;
    }

    void reset() {
      *$self = nullptr;
    }
  }
};
}
