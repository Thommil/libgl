#ifndef _Included_Matrix
#define _Included_Matrix

#define IDENTITY(_m) _m[1] = _m[2] = _m[3] = _m[4] = _m[6] = _m[7] = _m[8] = _m[9] = _m[11] = 0; _m[0] = _m[5] = _m[10] = _m[15] = 1;


#ifdef __cplusplus
extern "C" {
#endif

inline void mx4transform(float x, float y, float z, float w, const float* pM, float* pDest) {
    pDest[0] = pM[0 + 4 * 0] * x + pM[0 + 4 * 1] * y + pM[0 + 4 * 2] * z + pM[0 + 4 * 3] * w;
    pDest[1] = pM[1 + 4 * 0] * x + pM[1 + 4 * 1] * y + pM[1 + 4 * 2] * z + pM[1 + 4 * 3] * w;
    pDest[2] = pM[2 + 4 * 0] * x + pM[2 + 4 * 1] * y + pM[2 + 4 * 2] * z + pM[2 + 4 * 3] * w;
    pDest[3] = pM[3 + 4 * 0] * x + pM[3 + 4 * 1] * y + pM[3 + 4 * 2] * z + pM[3 + 4 * 3] * w;
}

bool invertM(float* mInv, const int mInvOffset, float* m, const int mOffset);

void multiplyMM(float* r, const float* lhs, const float* rhs);
void multiplyMV(float* r, const float* lhs, const float* rhs);

void rotateM(float* rm, int rmOffset, float angle, float x, float y, float z);
void scaleM(float* sm, int smOffset, float x, float y, float z);
void translateM(float* tm, const int tmOffset, float x, float y, float z);
void setLookAtM(float* rm, int rmOffset, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ);

#ifdef __cplusplus
}
#endif
#endif
