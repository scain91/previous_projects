// ------------------------------------
// projects/allocator/TestAllocator.c++
// Copyright (C) 2014
// Glenn P. Downing
// ------------------------------------

// --------
// includes
// --------

#include <algorithm> // count
#include <memory>    // allocator

#include "gtest/gtest.h"

#include "Allocator.h"

// -------------
// TestAllocator
// -------------

template <typename A>
struct TestAllocator : testing::Test {
    // --------
    // typedefs
    // --------

    typedef          A                  allocator_type;
    typedef typename A::value_type      value_type;
    typedef typename A::difference_type difference_type;
    typedef typename A::pointer         pointer;};

typedef testing::Types<
            std::allocator<int>,
            std::allocator<double>,
            std::allocator<char>,
            std::allocator<bool> >
        types;

TYPED_TEST_CASE(TestAllocator, types);

TYPED_TEST(TestAllocator, One) {
    typedef typename TestFixture::allocator_type  allocator_type;
    typedef typename TestFixture::value_type      value_type;
    typedef typename TestFixture::difference_type difference_type;
    typedef typename TestFixture::pointer         pointer;

    allocator_type x;
    const difference_type s = 1;
    const value_type      v = 2;
    const pointer         p = x.allocate(s);
    if (p != 0) {
        x.construct(p, v);
        ASSERT_EQ(v, *p);
        x.destroy(p);
        x.deallocate(p, s);
    }
}

TYPED_TEST(TestAllocator, Ten) {
    typedef typename TestFixture::allocator_type  allocator_type;
    typedef typename TestFixture::value_type      value_type;
    typedef typename TestFixture::difference_type difference_type;
    typedef typename TestFixture::pointer         pointer;

    allocator_type x;
    const difference_type s = 10;
    const value_type      v = 2;
    const pointer         b = x.allocate(s);
    if (b != 0) {
        pointer e = b + s;
        pointer p = b;
        try {
            while (p != e) {
                x.construct(p, v);
                ++p;
            }
        }
        catch (...) {
            while (b != p) {
                --p;
                x.destroy(p);}
            x.deallocate(b, s);
            throw;
        }
        ASSERT_EQ(s, std::count(b, e, v));
        while (b != e) {
            --e;
            x.destroy(e);}
        x.deallocate(b, s);
    }
}

// ----------------
// MyTestAllocator
// ----------------

template <typename A>
struct MyTestAllocator : testing::Test {
    // --------
    // typedefs
    // --------

    typedef          A                  allocator_type;
    typedef typename A::value_type      value_type;
    typedef typename A::difference_type difference_type;
    typedef typename A::pointer         pointer;};

typedef testing::Types<
            Allocator<int, 100>,
            Allocator<double, 100>, 
            Allocator<char, 100>,
            Allocator<bool, 100>,
            Allocator<float, 100> >
        my_types;

TYPED_TEST_CASE(MyTestAllocator, my_types);

// Constructor

// valid()

// allocate()
TYPED_TEST(MyTestAllocator, allocate_bad_alloc_zero) {
    typedef typename TestFixture::allocator_type  allocator_type;
    typedef typename TestFixture::value_type      value_type;
    typedef typename TestFixture::difference_type difference_type;
    typedef typename TestFixture::pointer         pointer;

   
    bool caught_exception = false;

    try{
    	allocator_type x;
    	const difference_type s = 0;
    	const value_type      v = 2;
    	const pointer         p = x.allocate(s);
    }
  	catch(...){
  		caught_exception = true;
  	}

  	ASSERT_TRUE(caught_exception);
}

TYPED_TEST(MyTestAllocator, allocate_bad_alloc_exceed1) {
    typedef typename TestFixture::allocator_type  allocator_type;
    typedef typename TestFixture::value_type      value_type;
    typedef typename TestFixture::difference_type difference_type;
    typedef typename TestFixture::pointer         pointer;

   
    bool caught_exception = false;

    try{
    	allocator_type x;
    	const difference_type s = 100;
    	const value_type      v = 2;
    	const pointer         p = x.allocate(s);
    }
  	catch(...){
  		caught_exception = true;
  	}

  	ASSERT_TRUE(caught_exception);
}

TYPED_TEST(MyTestAllocator, allocate_bad_alloc_exceed2) {
    typedef typename TestFixture::allocator_type  allocator_type;
    typedef typename TestFixture::value_type      value_type;
    typedef typename TestFixture::difference_type difference_type;
    typedef typename TestFixture::pointer         pointer;

   
    bool caught_exception = false;

    try{
    	allocator_type x;
    	const difference_type s = 93;
    	const value_type      v = 2;
    	const pointer         p = x.allocate(s);
    }
  	catch(...){
  		caught_exception = true;
  	}

  	ASSERT_TRUE(caught_exception);
}


// deallocate()
TYPED_TEST(MyTestAllocator, deallocate_throw1) {
    typedef typename TestFixture::allocator_type  allocator_type;
    typedef typename TestFixture::value_type      value_type;
    typedef typename TestFixture::difference_type difference_type;
    typedef typename TestFixture::pointer         pointer;

   
    bool caught_exception = false;

    try{
    	allocator_type x;
    	const difference_type s = 92;
    	const value_type      v = 2;
    	const pointer         p = x.allocate(s);

    	const pointer fail = 0;

    	x.deallocate(fail, s);
    }
  	catch(...){
  		caught_exception = true;
  	}

  	ASSERT_TRUE(caught_exception);
}


// ------------------------------
// Constructor Tests
// ------------------------------

// Verify constructor is throwing bad_alloc 
TEST(myAllocator, constructor_bad1){
    bool caught_exception = false;

    try{
        Allocator<int, 10> a;
    }
    catch(...){
        caught_exception = true;
    }

    ASSERT_TRUE(caught_exception);
}

TEST(myAllocator, constructor_bad2){
    bool caught_exception = false;

    try{
        Allocator<int, 1> a;
    }
    catch(...){
        caught_exception = true;
    }

    ASSERT_TRUE(caught_exception);
}

// Verify constructor accepts valid initial value
TEST(myAllocator, constructor_good1){
  bool caught_exception = false;

    try{
        Allocator<int, 12> a;
    }
    catch(...){
        caught_exception = true;
    }

    ASSERT_FALSE(caught_exception);   
}

TEST(myAllocator, constructor_good2){
  bool caught_exception = false;

    try{
        Allocator<double, 16> a;
    }
    catch(...){
        caught_exception = true;
    }

    ASSERT_FALSE(caught_exception);
}

// ---------------
// valid() tests
// ---------------
TEST(myAllocator, valid_empty1){

    Allocator<int, 12>  a;
    const Allocator<int, 12>& ca = a;

    ASSERT_EQ(ca.view(0), 4);
}

TEST(myAllocator, valid_nonempty1){

    Allocator<int, 12>  a;
    a.allocate(1);

    const Allocator<int, 12>& ca = a;

    ASSERT_EQ(ca.view(0), -4);
}

TEST(myAllocator, valid_nonempty2){

    Allocator<int, 12>  a;
    a.allocate(1);

    const Allocator<int, 12>& ca = a;

    ASSERT_EQ(ca.view(0), -4);
    ASSERT_EQ(ca.view(8), -4);
}

// Verify sentinels placed correctly following constructor 
TEST(myAllocator, sentinel_good1){

    int total_size = 100;
    int sent_size = 8;

    Allocator<int, 100>  a;
    const Allocator<int, 100>& ca = a;

    ASSERT_EQ(ca.view(0), total_size-sent_size);
    ASSERT_EQ(ca.view(total_size-4), total_size-sent_size);
}


// More
//allocate 3 items, leave middle and reallocate first item
TYPED_TEST(TestAllocator, Three) { 
    using namespace std;

    typedef typename TestFixture::allocator_type  allocator_type;
    typedef typename TestFixture::value_type      value_type;
    typedef typename TestFixture::difference_type difference_type;
    typedef typename TestFixture::pointer         pointer;

    allocator_type x;
    const difference_type s5 = 2;
    const difference_type s9 = 4;
    const value_type      v = 2;
    const pointer         a = x.allocate(s5);
    const pointer         b = x.allocate(s5);
    const pointer         c = x.allocate(s9);


    if (a != 0) {
        x.construct(a, v);
        ASSERT_EQ(v, *a);
    }
    if (b != 0) {
        x.construct(b, v);
        ASSERT_EQ(v, *b);
    }
    if (c != 0) {
        x.construct(c, v);
        ASSERT_EQ(v, *c);
    }

    //cout << endl << "deallocate a" << endl;
    x.deallocate(a, s5);
    //cout << endl << "deallocate c" << endl;
    //x.deallocate(c, s9);

    const pointer a2 = x.allocate(s5);
    if (a2 != 0) {
        x.construct(a2, v);
        ASSERT_EQ(v, *a2);
    }

}


TYPED_TEST(TestAllocator, ThreeTeen) { 
    using namespace std;

    typedef typename TestFixture::allocator_type  allocator_type;
    typedef typename TestFixture::value_type      value_type;
    typedef typename TestFixture::difference_type difference_type;
    typedef typename TestFixture::pointer         pointer;

    allocator_type x;
    const difference_type s5 = 2;
    const difference_type s9 = 4;
    const value_type      v = 2;

    for(int i = 0; i < 200; ++i) {
        const pointer         a = x.allocate(s5);
        const pointer         b = x.allocate(s5);
        const pointer         c = x.allocate(s9);


        if (a != 0) {
            x.construct(a, v);
            ASSERT_EQ(v, *a);
        }
        if (b != 0) {
            x.construct(b, v);
            ASSERT_EQ(v, *b);
        }
        if (c != 0) {
            x.construct(c, v);
            ASSERT_EQ(v, *c);
        }

        //cout << endl << "deallocate a" << endl;
        x.deallocate(a, s5);
        //cout << endl << "deallocate c" << endl;
        x.deallocate(c, s9);
        x.deallocate(b, s5);
    }

}


TYPED_TEST(TestAllocator, Four) { 
    using namespace std;

    typedef typename TestFixture::allocator_type  allocator_type;
    typedef typename TestFixture::value_type      value_type;
    typedef typename TestFixture::difference_type difference_type;
    typedef typename TestFixture::pointer         pointer;

    allocator_type x;
    const difference_type s1 = 4;
    const difference_type s2 = 5;
    const value_type      v = 22;



    const pointer         a = x.allocate(s1);
    const pointer         b = x.allocate(s2);
    //const pointer         c = x.allocate(s9);

    pointer temp = a;
    for(int i = 0; i < 100; ++i) {
        x.deallocate(temp, s1);
        temp = x.allocate(s1);
        x.construct(temp, v);

    }

    if (temp != 0) {
        ASSERT_EQ(v, *temp);
    }

}

TYPED_TEST(TestAllocator, Five) { 
    using namespace std;

    typedef typename TestFixture::allocator_type  allocator_type;
    typedef typename TestFixture::value_type      value_type;
    typedef typename TestFixture::difference_type difference_type;
    typedef typename TestFixture::pointer         pointer;

    allocator_type x;
    const difference_type s5 = 2;
    const difference_type s9 = 4;
    const value_type      v = 2;
    const pointer         a = x.allocate(s5);
    const pointer         b = x.allocate(s5);
    const pointer         c = x.allocate(s9);


    if (a != 0) {
        x.construct(a, v);
        ASSERT_EQ(v, *a);
    }
    if (b != 0) {
        x.construct(b, v);
        ASSERT_EQ(v, *b);
    }
    if (c != 0) {
        x.construct(c, v);
        ASSERT_EQ(v, *c);
    }

    //cout << endl << "deallocate a" << endl;
    x.deallocate(b, s5);
    //cout << endl << "deallocate c" << endl;
    //x.deallocate(c, s9);

    const pointer        b2 = x.allocate(s5);
    if (b2 != 0) {
        x.construct(b2, v);
        ASSERT_EQ(v, *b2);
    }
    else {
        ASSERT_TRUE(false);
    }
}

TYPED_TEST(TestAllocator, FiveTeen) { 
    using namespace std;

    typedef typename TestFixture::allocator_type  allocator_type;
    typedef typename TestFixture::value_type      value_type;
    typedef typename TestFixture::difference_type difference_type;
    typedef typename TestFixture::pointer         pointer;

    allocator_type x;
    const difference_type s5 = 3;
    const difference_type s9 = 5;
    const value_type      v = 9;
    const pointer         a = x.allocate(s5);
    const pointer         b = x.allocate(s5);
    const pointer         c = x.allocate(s9);


    if (a != 0) {
        x.construct(a, v);
        ASSERT_EQ(v, *a);
    }
    if (b != 0) {
        x.construct(b, v);
        ASSERT_EQ(v, *b);
    }
    if (c != 0) {
        x.construct(c, v);
        ASSERT_EQ(v, *c);
    }

    x.deallocate(b, s5);
    x.deallocate(a, s5);

    const pointer        b2 = x.allocate(s5);
    if (b2 != 0) {
        x.construct(b2, v);
        ASSERT_EQ(v, *b2);
    }
    else {
        ASSERT_TRUE(false);
    }
}

TEST(MyAllocator, test1) {

    try {
        Allocator<int,100> x;
        x.allocate(500);

    }
    catch (std::bad_alloc) {
        ASSERT_TRUE(true);
        return;
    }

    ASSERT_TRUE(false);
}