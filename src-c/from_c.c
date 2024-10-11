#include <libsci.h>
#include <stdio.h>
#include <string.h>

extern long long __global_graal_thread;

// save function from Clojure side
int (*cmp_fn)(const long long, const double, const double);

int cmp_double(const void *v0, const void *v1) {
  double fv0 = *(const double *)v0;
  double fv1 = *(const double *)v1;
  return cmp_fn(__global_graal_thread, fv0, fv1);
}

void qsort1(void *buf, size_t n, size_t s, void *comp) {
  // function from GraalVM takes an extra thread isolate argument.
  //
  // for demo purpose, we call into the function through `cmp_double` wrapper.
  cmp_fn = (int (*)(const long long, const double, const double))comp;
  qsort(buf, n, s, cmp_double);
}

int main(int argc, char *argv[]) {
  graal_isolate_t *isolate = NULL;
  graal_isolatethread_t *thread = NULL;

  if (graal_create_isolate(NULL, &isolate, &thread) != 0) {
    fprintf(stderr, "initialization error\n");
    return 1;
  }

  // setup for `cmp_double`
  __global_graal_thread = (long long)thread;

  /* printf("hello: %lld\n", hello(thread)); */
  printf("C -> clj: add 4 2: %lld\n", add(thread, 4, 2));
  printf("C -> clj: cmp 4 2: %d, cmp 2 4: %d\n", cmp(thread, 4, 2),
         cmp(thread, 2, 4));

  printf("C: -- enter sci --\n");
  const char input[4096] = "";
  for (int index = 1; index < argc; index++) {
    strcat((char *)input, argv[index]);
    strcat((char *)input, " ");
  }
  char *result = eval_string((long long)thread, input);
  printf("C: -- leave sci --\n");
  printf("%s\n", result);
  return 0;
}
