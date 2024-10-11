# dt-ffi

```sh
bb clj:compile
bb c:make clean all

./target/native/from_c $(read -z)
# input expressions & Ctrl-D to submit

# call into C with function pointer
./target/native/from_c '(demo/lib0-test-with-graall-binding)'
./target/native/from_c '(demo/lib1-test-with-graall-binding)'

# these won't work for now
./target/native/from_c '(demo/lib0-test-with-libpath)'
./target/native/from_c '(demo/lib1-test-with-libpath)'
./target/native/from_c '(demo/lib0-ffi)'
```
