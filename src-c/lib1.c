#include <stdio.h>
#include <stdlib.h>

/* address */
extern long long __global_graal_thread;

int (*cmp_fn)(const long long, const double, const double);

int cmp_double(const void *v0, const void *v1) {
  double fv0 = *(const double *)v0;
  double fv1 = *(const double *)v1;
  return cmp_fn(__global_graal_thread, fv0, fv1);
}

void qsort1(void *buf, size_t n, size_t s, void *comp) {
  cmp_fn = (int (*)(const long long, const double, const double))comp;
  printf("\nhello world 0: v0=%f\n", *(const double *)buf);
  qsort(buf, n, s, cmp_double);
  printf("hello world 1: v0=%f\n\n", *(const double *)buf);
}
