// ------------------------------
// projects/allocator/Allocator.h
// Copyright (C) 2014
// Glenn P. Downing
// ------------------------------

#ifndef Allocator_h
#define Allocator_h

// DEBUG FLAG
#define DEBUG 0

// --------
// includes
// --------

#include <cassert>   // assert
#include <cstddef>   // ptrdiff_t, size_t
#include <new>       // bad_alloc, new
#include <stdexcept> // invalid_argument
#include <cmath>     // abs

// ---------
// Allocator
// ---------

template <typename T, int N>
class Allocator {
    public:
        // --------
        // typedefs
        // --------

        typedef T                 value_type;

        typedef std::size_t       size_type;
        typedef std::ptrdiff_t    difference_type;

        typedef       value_type*       pointer;
        typedef const value_type* const_pointer;

        typedef       value_type&       reference;
        typedef const value_type& const_reference;

    public:
        // -----------
        // operator ==
        // -----------

        friend bool operator == (const Allocator&, const Allocator&) { //don't touch this
            return true;                             // this is correct
        }                                             

        // -----------
        // operator !=
        // -----------

        friend bool operator != (const Allocator& lhs, const Allocator& rhs) {
            return !(lhs == rhs);
        }

    private:
        // ----
        // data
        // ----

        char a[N]; //given

        // -----
        // valid
        // -----

        /**
         * O(1) in space
         * O(n) in time
         * <your documentation>
         * make sure my heap is in a proper state
         * go through entire heap array and make sure it's in a correct state
         * check that all sentinels and blocks in between are correct
         * use view to validate each sentinel, then go through each allocated block by sizeof(T) checking that the correct number are allocated
         * check that a matching sentinel is at the end of the allocated block
         * check that at the end of the heap array there is enough space for 2 sentinels and 1 sizeof(T)
         * if it is not valid, just return false 
         */
        bool valid () const {
            if(DEBUG) std::cout<< "Starting valid" << std::endl;
            int i = 0;
            while(i < N) {
                if(DEBUG) std::cout<< "Starting valid() while loop, currently on index: " << i << std::endl;
                //look at each spot in array and check
                int beg_sen_value = view(i); //call public view with const something
                if(DEBUG) std::cout<< "beg_sen_value " << beg_sen_value << std::endl;
                int pos_beg_sen_value = std::abs(beg_sen_value);
                if(pos_beg_sen_value > (N-i-8)) { //check first sentinel
                    return false;
                }

                int end_sen_value = view(i+pos_beg_sen_value+4);
                if(DEBUG) std::cout<< "end_sen_value " << end_sen_value << std::endl;
                if(end_sen_value != beg_sen_value) { //check second sentinel
                    return false;
                }
                i = i + pos_beg_sen_value + 8;
            }
            if(DEBUG) std::cout<< "Finished valid()" << std::endl;
            return true;
        }

        /**
         * O(1) in space
         * O(1) in time
         * <your documentation>
         * 
         */
        int& view (int i) { //don't change this
            return *reinterpret_cast<int*>(&a[i]);
        }

    public:
        // ------------
        // constructors
        // ------------

        /**
         * O(1) in space
         * O(1) in time
         * throw a bad_alloc exception, if N is less than sizeof(T) + (2 * sizeof(int))
         */
        Allocator () {
            //set up heap here
            //try to assign sentinels to beginning and end of heap
            //throw a bad_alloc exception, if N is less than sizeof(T) + (2 * sizeof(int))
            if(DEBUG) std::cout<< "Starting Allocator()" << std::endl;

            // validate n, throw exception
            if(N < (sizeof(T) + (2 * sizeof(int)))) {
                    throw std::bad_alloc();
            }
       
            if(DEBUG) std::cout<< "Past Allocator() try catch" << std::endl;
            //assign sentinels
            view(0) = N - 8;
            view(N-4) = N - 8;
            if(DEBUG) std::cout<< "Allocator() allocated the sentinels" << std::endl;
            

            // possibly not catch?
            // catch(std::bad_alloc& ba) {
            //     return ba;                          // check return type
            // }

            assert(valid()); //assert that heap is in a valid state before allocating
        }

        // Default copy, destructor, and copy assignment
        // Allocator  (const Allocator&);
        // ~Allocator ();
        // Allocator& operator = (const Allocator&);

        // --------
        // allocate
        // --------

        /**
         * O(1) in space
         * O(n) in time
         * after allocation there must be enough space left for a valid block
         * the smallest allowable block is sizeof(T) + (2 * sizeof(int))
         * choose the first block that fits
         * return 0, if allocation fails
         */
        pointer allocate (size_type n) { //number of type T elements, not number of bytes
            if(DEBUG) std::cout<< "Starting allocate()" << std::endl;
            if(DEBUG) std::cout<< "allocate() n elements = " << n << std::endl;
            
            assert(valid()); //assert heap is in valid state before allocating

            // check for n invalidity 
            if(n <= 0 || n*sizeof(T)+2*sizeof(int) > N ){
                throw std::bad_alloc();
            }

            int n_element_bytes = sizeof(T)*n;                                  // size of elements in bytes
            int min_required_blocks = sizeof(T) + (2 * sizeof(int));            // total size required for allocation 
            if(DEBUG) std::cout<< "allocate() n_element_bytes " << n_element_bytes << std::endl;

            // Iterate through heap 
            // find first block that it will fit
            int i = 0;
            while(i < N) { //i is the current first index of the beginning sentinel for a pair of sentinels
                //go through entire heap until you find a spot
                //use view, if free and >= alloc_size, allocate that block of space
                int beg_sen_value = view(i);
                int pos_beg_sen_value = std::abs(beg_sen_value);
                if(DEBUG) std::cout<< "allocate() beg_sen_value for i " << beg_sen_value << std::endl;
                if(beg_sen_value > 0 && beg_sen_value >= n_element_bytes) {
                    if((beg_sen_value - n_element_bytes) >= min_required_blocks) {
                        //need to free up the extras
                        //change first sentinel to -n_element_bytes
                        if(DEBUG) std::cout<< "allocate() if" << std::endl;
                        view(i) = -n_element_bytes; 
                        if(DEBUG) std::cout<< "allocate() first sentinel " << view(i) << std::endl;
                        //add new second sentinel at i+4+n_element_bytes to -n_element_bytes
                        view(i+4+n_element_bytes) = -n_element_bytes;
                        if(DEBUG) std::cout<< "allocate() second sentinel " << view(i+4+n_element_bytes) << std::endl;
                        //add new first sentinel at i+n_element_bytes+8 to pos begin-n_element_bytes-8
                        view(i+8+n_element_bytes) = pos_beg_sen_value - n_element_bytes - 8;
                        if(DEBUG) std::cout<< "allocate() third sentinel " << view(i+8+n_element_bytes) << std::endl;
                        //change last sentinel at i+4+pos_begin to begin-n_element_bytes-8
                        view(i+4+beg_sen_value) = pos_beg_sen_value - n_element_bytes - 8;
                        if(DEBUG) std::cout<< "allocate() fourth sentinel " << view(i+4+beg_sen_value) << std::endl;
                    }
                    else {
                        //just allocate entire block (change sentinels to negative)
                        if(DEBUG) std::cout<< "allocate() else " << std::endl;
                        if(DEBUG) std::cout<< "allocate() beg_sen_value " << beg_sen_value << std::endl;
                        if(DEBUG) std::cout<< "allocate() i " << i << std::endl;
                        int end_sen_index = i + beg_sen_value + 4;
                        view(i) = -beg_sen_value; //n_element_bytes;
                        view(end_sen_index) = -beg_sen_value; //n_element_bytes;
                        if(DEBUG) std::cout<< "allocate() first sentinel index " << i << std::endl;
                        if(DEBUG) std::cout<< "allocate() second sentinel index " << end_sen_index << std::endl;
                        if(DEBUG) std::cout<< "allocate() first sentinel value " << view(i) << std::endl;
                        if(DEBUG) std::cout<< "allocate() second sentinel value " << view(i+4+beg_sen_value) << std::endl;
                    }

                    return (pointer) &a[i+4];   // return ptr to first block of allocated values (not sentinels)
                                                // check cast

                }
                else {
                    //keep looking
                    i = i + pos_beg_sen_value + 8;
                }
            }

            if(DEBUG) std::cout<< "Past allocate() while loop" << std::endl;
            //return pointer to first allocated block byte (not sentinel)
            assert(valid()); //assert that heap is in a valid state after we have allocated something
            if(DEBUG) std::cout<< "Finished allocate()" << std::endl;
            return 0;  //return 0 if allocation fails, otherwise return pointer to first byte allocated
        }                   

        // ---------
        // construct
        // ---------

        /**
         * O(1) in space
         * O(1) in time
         * <your documentation>
         * construct is putting value v into memory where p is pointing
         * user will handle this, not our problem
         */
        void construct (pointer p, const_reference v) { //don't touch this
            new (p) T(v);                               // this is correct and exempt
            assert(valid());                            // from the prohibition of new
        }                           

        // ----------
        // deallocate
        // ----------

        /**
         * O(1) in space
         * O(1) in time
         * after deallocation adjacent free blocks must be coalesced
         * <your documentation>
         */
        void deallocate (pointer p, size_type) {
            assert(valid());
            //throw invalid arg if ptr is invalid
            if(((char *)p) < &a[0] || ((char *)p) > &a[N]) {
                throw std::invalid_argument("");
            }

            assert(valid());

            //p is pointer to first address byte of blocks (not sentinels)
            //go to spot p in heap, get size from sentinel (view(p - 4))
            //go spot p + size to get to other sentinel
            //make both sentinels positive
            //check prev sentinel and next sentinel to see if they are free, if they are, coelesce
            //to coelesce, delete middle sentinels and change value in end sentinels

            // Todo: Check validity of pointer p

            //get address of index 0 in array
            //(pointer p address - index 0 address)/( 8 bits/ byte) - 1 = number of bytes/index spots p is
            if(DEBUG) std::cout<< "Starting deallocate()" << std::endl;
            pointer index_zero_addr = (pointer) &a[0];
            if(DEBUG) std::cout<< "deallocate() pointer to index_zero_addr " << index_zero_addr << std::endl;
            if(DEBUG) std::cout<< "deallocate() pointer to p " << p << std::endl;
            
            int p_index = ((char *)p) - &(a[0]) - 4;
            if(DEBUG) std::cout<< "deallocate() p_index " << p_index << std::endl;

            // local sentinel values 
            int original_sen_value = view(p_index);
            if(DEBUG) std::cout<< "deallocate() original_sen_value " << original_sen_value << std::endl;

            // local indices 
            int original_beg_sen_index = p_index;
            if(DEBUG) std::cout<< "deallocate()  original_beg_sen_index " << original_beg_sen_index << std::endl;
            int original_end_sen_index = original_beg_sen_index + std::abs(original_sen_value) + 4;
            if(DEBUG) std::cout<< "deallocate()  original_end_sen_index " << original_end_sen_index << std::endl;
            int current_beg_sen_index = original_beg_sen_index; //this will end up being the beginning sen index if the previous block can be coelesced
            if(DEBUG) std::cout<< "deallocate()  current_beg_sen_index " << current_beg_sen_index << std::endl;
            int current_end_sen_index = original_end_sen_index; //this will end up being the end sen index if the next block can be coelesced
            if(DEBUG) std::cout<< "deallocate()  current_end_sen_index " << current_end_sen_index << std::endl;

            // keep track of new total free block size, this will be the value assigned to sentinels
            int free_blocks = std::abs(original_sen_value);
            if(DEBUG) std::cout<< "deallocate() starting free_blocks " << free_blocks << std::endl;

            // Check for coalescing 
            // cases: 
                // coalesce none, before, after, both
                // check for out of bounds index

            // if beg_sen_value > 0 then check 4 indices behind
            if(original_beg_sen_index > 0) { //check if this is the beginning of array
                if(DEBUG) std::cout<< "deallocate() original_beg_sen_index " << original_beg_sen_index << " > 0 " << std::endl;
                int prev_sen_end_index = current_beg_sen_index - 4;
                int prev_sen_value = view(prev_sen_end_index);

                if(prev_sen_value > 0 ) { //it's free, going to coelesce
                    // add freed blocks to total free blocks + 8
                    if(DEBUG) std::cout<< "deallocate() prev_sen_value " << prev_sen_value << " > 0"  << std::endl;
                    free_blocks += prev_sen_value + 8;
                    current_beg_sen_index = current_beg_sen_index - prev_sen_value - 8; //do not change this back to -=
                }
                if(DEBUG) std::cout<< "deallocate() current_beg_sen_index " << current_beg_sen_index << std::endl;
                if(DEBUG) std::cout<< "deallocate() free_blocks " << free_blocks << std::endl;
            }

            // if (end_sent_value + 4) < N then check after 
                        // if after sent is positive
                            // add freed blocks to total free blocks + 8
                            // new end_sen_value = next_sen_index - next_sen_value + 4

            //if original_end_sen_index < N - 4, then check 4 indices ahead
            if(original_end_sen_index + 4 < N) {
                if(DEBUG) std::cout<< "deallocate() original_end_sen_index + 4 < N " << std::endl;
                int next_sen_beg_index = original_end_sen_index + 4;
                int next_sen_value = view(next_sen_beg_index);

                if(next_sen_value > 0) { //if true, it's free, coelesce
                    if(DEBUG) std::cout<< "deallocate() next_sen_value " << next_sen_value << " > 0" << std::endl;
                    free_blocks += next_sen_value + 8;
                    current_end_sen_index += next_sen_value + 8;
                }
                if(DEBUG) std::cout<< "deallocate() current_end_sen_index " << current_end_sen_index << std::endl;
                if(DEBUG) std::cout<< "deallocate() free_blocks " << free_blocks << std::endl;
            }
            
            //assign the sentinels
            view(current_beg_sen_index) = free_blocks;
            view(current_end_sen_index) = free_blocks;
            if(DEBUG) std::cout<< "deallocate() final current_beg_sen_index " << current_beg_sen_index << std::endl;
            if(DEBUG) std::cout<< "deallocate() final current_end_sen_index " << current_end_sen_index << std::endl;

            assert(valid());
            if(DEBUG) std::cout<< "deallocate() Finished deallocate " << std::endl;
        }

        // -------
        // destroy
        // -------

        /**
         * O(1) in space
         * O(1) in time
         * throw an invalid_argument exception, if pointer is invalid
         * <your documentation>
         * user will handle this, not our problem
         */
        void destroy (pointer p) {  //don't touch this
            p->~T();               // this is correct
            assert(valid());    
        }

        /**
         * O(1) in space
         * O(1) in time
         * <your documentation>
         * use this to read and write all of the sentinels
         * returns a integer reference of the data stored at a given index "i". 
         * "i" is the address of they first byt of the sentinel
         * view converts "i" to a reference to an int which is the sentinel value
         *
         * to assign a value to a sentinel: view(i) = value
         * Assigning that reference will write a full int, starting at the byte indexed initially, then the next 3 bytes for a total of 4 bytes
         */
        const int& view (int i) const { //don't touch this
            return *reinterpret_cast<const int*>(&a[i]);
        }
    };

#endif // Allocator_h