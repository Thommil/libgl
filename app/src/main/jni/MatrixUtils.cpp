#include "MatrixUtils.h"
#include <math.h>
//#include <android/log.h> //__android_log_write(ANDROID_LOG_DEBUG,TAG,MSG);

#define I(_i, _j) ((_j)+ 4*(_i))

void multiplyMM(float* r, const float* lhs, const float* rhs)
{
    for (int i=0 ; i<4 ; i++) {
        register const float rhs_i0 = rhs[ I(i,0) ];
        register float ri0 = lhs[ I(0,0) ] * rhs_i0;
        register float ri1 = lhs[ I(0,1) ] * rhs_i0;
        register float ri2 = lhs[ I(0,2) ] * rhs_i0;
        register float ri3 = lhs[ I(0,3) ] * rhs_i0;
        for (int j=1 ; j<4 ; j++) {
            register const float rhs_ij = rhs[ I(i,j) ];
            ri0 += lhs[ I(j,0) ] * rhs_ij;
            ri1 += lhs[ I(j,1) ] * rhs_ij;
            ri2 += lhs[ I(j,2) ] * rhs_ij;
            ri3 += lhs[ I(j,3) ] * rhs_ij;
        }
        r[ I(i,0) ] = ri0;
        r[ I(i,1) ] = ri1;
        r[ I(i,2) ] = ri2;
        r[ I(i,3) ] = ri3;
    }
}

void multiplyMV(float* r, const float* lhs, const float* rhs)
{
    mx4transform(rhs[0], rhs[1], rhs[2], rhs[3], lhs, r);
}

void scaleM(float* sm, int smOffset, float x, float y, float z){
	for (int i=0 ; i<4 ; i++) {
		int smi = smOffset + i;
		*(sm + smi) *= x;
		*(sm + 4 + smi) *= y;
		*(sm + 8 + smi) *= z;
	}
}

void translateM(float* tm, const int tmOffset, float x, float y, float z){
	for (int i=0 ; i<4 ; i++) {
		int tmi = tmOffset + i;
		*(tm + 12 + tmi) += *(tm + tmi) * x + *(tm + 4 + tmi) * y + *(tm + 8 + tmi) * z;
	}
}

void rotateM(float* rm, int rmOffset, float angle, float x, float y, float z){
	*(rm + rmOffset + 3) = 0;
	*(rm + rmOffset + 7) = 0;
	*(rm + rmOffset + 11) = 0;
	*(rm + rmOffset + 12) = 0;
	*(rm + rmOffset + 13) = 0;
	*(rm + rmOffset + 14) = 0;
	*(rm + rmOffset + 15) = 1;
	angle *= (float) (M_PI / 180.0f);
	float s = (float) sin(angle);
	float c = (float) cos(angle);
	if (1.0f == x && 0.0f == y && 0.0f == z) {
		*(rm + rmOffset + 5) = c;   *(rm + rmOffset + 10) = c;
		*(rm + rmOffset + 6) = s;   *(rm + rmOffset + 9) = -s;
		*(rm + rmOffset + 1) = 0;   *(rm + rmOffset + 2) = 0;
		*(rm + rmOffset + 4) = 0;   *(rm + rmOffset + 8) = 0;
		*(rm + rmOffset + 0) = 1;
	} else if (0.0f == x && 1.0f == y && 0.0f == z) {
		*(rm + rmOffset + 0) = c;   *(rm + rmOffset + 10) = c;
		*(rm + rmOffset + 8) = s;   *(rm + rmOffset + 2) = -s;
		*(rm + rmOffset + 1) = 0;   *(rm + rmOffset + 4) = 0;
		*(rm + rmOffset + 6) = 0;   *(rm + rmOffset + 9) = 0;
		*(rm + rmOffset + 5) = 1;
	} else if (0.0f == x && 0.0f == y && 1.0f == z) {
		*(rm + rmOffset + 0) = c;   *(rm + rmOffset + 5) = c;
		*(rm + rmOffset + 1) = s;   *(rm + rmOffset + 4) = -s;
		*(rm + rmOffset + 2) = 0;   *(rm + rmOffset + 6) = 0;
		*(rm + rmOffset + 8) = 0;   *(rm + rmOffset + 9) = 0;
		*(rm + rmOffset + 10)= 1;
	} else {
		float len =  (float) sqrt(x * x + y * y + z * z);
		if (1.0f != len) {
			float recipLen = 1.0f / len;
			x *= recipLen;
			y *= recipLen;
			z *= recipLen;
		}
		float nc = 1.0f - c;
		float xy = x * y;
		float yz = y * z;
		float zx = z * x;
		float xs = x * s;
		float ys = y * s;
		float zs = z * s;
		*(rm + rmOffset + 0) = x*x*nc +  c;
		*(rm + rmOffset + 4) =  xy*nc - zs;
		*(rm + rmOffset + 8) =  zx*nc + ys;
		*(rm + rmOffset + 1) =  xy*nc + zs;
		*(rm + rmOffset + 5) = y*y*nc +  c;
		*(rm + rmOffset + 9) =  yz*nc - xs;
		*(rm + rmOffset + 2) =  zx*nc - ys;
		*(rm + rmOffset + 6) =  yz*nc + xs;
		*(rm + rmOffset + 10) = z*z*nc +  c;
	}
}

void setLookAtM(float* rm, int rmOffset, float eyeX, float eyeY, float eyeZ,
											float centerX, float centerY, float centerZ,
											float upX, float upY, float upZ)
{
	float fx = centerX - eyeX;
	float fy = centerY - eyeY;
	float fz = centerZ - eyeZ;

	// Normalize f
	float rlf = 1.0f / (float) sqrt(fx * fx + fy * fy + fz * fz);
	fx *= rlf;
	fy *= rlf;
	fz *= rlf;

	// compute s = f x up (x means "cross product")
	float sx = fy * upZ - fz * upY;
	float sy = fz * upX - fx * upZ;
	float sz = fx * upY - fy * upX;

	// and normalize s
	float rls = 1.0f / (float) sqrt(sx * sx + sy * sy + sz * sz);
	sx *= rls;
	sy *= rls;
	sz *= rls;

	// compute u = s x f
	float ux = sy * fz - sz * fy;
	float uy = sz * fx - sx * fz;
	float uz = sx * fy - sy * fx;

	rm[rmOffset + 0] = sx;
	rm[rmOffset + 1] = ux;
	rm[rmOffset + 2] = -fx;
	rm[rmOffset + 3] = 0.0f;

	rm[rmOffset + 4] = sy;
	rm[rmOffset + 5] = uy;
	rm[rmOffset + 6] = -fy;
	rm[rmOffset + 7] = 0.0f;

	rm[rmOffset + 8] = sz;
	rm[rmOffset + 9] = uz;
	rm[rmOffset + 10] = -fz;
	rm[rmOffset + 11] = 0.0f;

	rm[rmOffset + 12] = 0.0f;
	rm[rmOffset + 13] = 0.0f;
	rm[rmOffset + 14] = 0.0f;
	rm[rmOffset + 15] = 1.0f;

	translateM(rm, rmOffset, -eyeX, -eyeY, -eyeZ);
}


bool invertM(float* mInv, const int mInvOffset, float* m, const int mOffset)
{
	//Init
	static float srcMatrix[16];
	static float atmp[12];
	static float btmp[12];
	static float dstMatrix[16];
	static float invdet, det;

	// transpose matrix
	srcMatrix[0] = *(m + mOffset +  0);
	srcMatrix[4] = *(m + mOffset +  1);
	srcMatrix[8] = *(m + mOffset +  2);
	srcMatrix[12] = *(m + mOffset +  3);
	srcMatrix[1] = *(m + mOffset +  4);
	srcMatrix[5] = *(m + mOffset +  5);
	srcMatrix[9] = *(m + mOffset +  6);
	srcMatrix[13] = *(m + mOffset +  7);
	srcMatrix[2] = *(m + mOffset +  8);
	srcMatrix[6] = *(m + mOffset +  9);
	srcMatrix[10] = *(m + mOffset +  10);
	srcMatrix[14] = *(m + mOffset +  11);
	srcMatrix[3] = *(m + mOffset +  12);
	srcMatrix[7] = *(m + mOffset +  13);
	srcMatrix[11] = *(m + mOffset +  14);
	srcMatrix[15] = *(m + mOffset +  15);

	// calculate pairs for first 8 elements (cofactors)
	atmp[0]  = srcMatrix[10] * srcMatrix[15];
	atmp[1]  = srcMatrix[11] * srcMatrix[14];
	atmp[2]  = srcMatrix[9]  * srcMatrix[15];
	atmp[3]  = srcMatrix[11] * srcMatrix[13];
	atmp[4]  = srcMatrix[9]  * srcMatrix[14];
	atmp[5]  = srcMatrix[10] * srcMatrix[13];
	atmp[6]  = srcMatrix[8]  * srcMatrix[15];
	atmp[7]  = srcMatrix[11] * srcMatrix[12];
	atmp[8]  = srcMatrix[8]  * srcMatrix[14];
	atmp[9]  = srcMatrix[10] * srcMatrix[12];
	atmp[10] = srcMatrix[8]  * srcMatrix[13];
	atmp[11] = srcMatrix[9]  * srcMatrix[12];

	// calculate first 8 elements (cofactors)
	dstMatrix[0]  = (atmp[0] * srcMatrix[5] + atmp[3] * srcMatrix[6] + atmp[4]  * srcMatrix[7])
					  - (atmp[1] * srcMatrix[5] + atmp[2] * srcMatrix[6] + atmp[5]  * srcMatrix[7]);
	dstMatrix[1]  = (atmp[1] * srcMatrix[4] + atmp[6] * srcMatrix[6] + atmp[9]  * srcMatrix[7])
					  - (atmp[0] * srcMatrix[4] + atmp[7] * srcMatrix[6] + atmp[8]  * srcMatrix[7]);
	dstMatrix[2]  = (atmp[2] * srcMatrix[4] + atmp[7] * srcMatrix[5] + atmp[10] * srcMatrix[7])
					  - (atmp[3] * srcMatrix[4] + atmp[6] * srcMatrix[5] + atmp[11] * srcMatrix[7]);
	dstMatrix[3]  = (atmp[5] * srcMatrix[4] + atmp[8] * srcMatrix[5] + atmp[11] * srcMatrix[6])
					  - (atmp[4] * srcMatrix[4] + atmp[9] * srcMatrix[5] + atmp[10] * srcMatrix[6]);
	dstMatrix[4]  = (atmp[1] * srcMatrix[1] + atmp[2] * srcMatrix[2] + atmp[5]  * srcMatrix[3])
					  - (atmp[0] * srcMatrix[1] + atmp[3] * srcMatrix[2] + atmp[4]  * srcMatrix[3]);
	dstMatrix[5]  = (atmp[0] * srcMatrix[0] + atmp[7] * srcMatrix[2] + atmp[8]  * srcMatrix[3])
					  - (atmp[1] * srcMatrix[0] + atmp[6] * srcMatrix[2] + atmp[9]  * srcMatrix[3]);
	dstMatrix[6]  = (atmp[3] * srcMatrix[0] + atmp[6] * srcMatrix[1] + atmp[11] * srcMatrix[3])
					  - (atmp[2] * srcMatrix[0] + atmp[7] * srcMatrix[1] + atmp[10] * srcMatrix[3]);
	dstMatrix[7]  = (atmp[4] * srcMatrix[0] + atmp[9] * srcMatrix[1] + atmp[10] * srcMatrix[2])
					  - (atmp[5] * srcMatrix[0] + atmp[8] * srcMatrix[1] + atmp[11] * srcMatrix[2]);

	// calculate pairs for second 8 elements (cofactors)
	btmp[0]  = srcMatrix[2] * srcMatrix[7];
	btmp[1]  = srcMatrix[3] * srcMatrix[6];
	btmp[2]  = srcMatrix[1] * srcMatrix[7];
	btmp[3]  = srcMatrix[3] * srcMatrix[5];
	btmp[4]  = srcMatrix[1] * srcMatrix[6];
	btmp[5]  = srcMatrix[2] * srcMatrix[5];
	btmp[6]  = srcMatrix[0] * srcMatrix[7];
	btmp[7]  = srcMatrix[3] * srcMatrix[4];
	btmp[8]  = srcMatrix[0] * srcMatrix[6];
	btmp[9]  = srcMatrix[2] * srcMatrix[4];
	btmp[10] = srcMatrix[0] * srcMatrix[5];
	btmp[11] = srcMatrix[1] * srcMatrix[4];

	// calculate second 8 elements (cofactors)
	dstMatrix[8]  = (btmp[0]  * srcMatrix[13] + btmp[3]  * srcMatrix[14] + btmp[4]  * srcMatrix[15])
					  - (btmp[1]  * srcMatrix[13] + btmp[2]  * srcMatrix[14] + btmp[5]  * srcMatrix[15]);
	dstMatrix[9]  = (btmp[1]  * srcMatrix[12] + btmp[6]  * srcMatrix[14] + btmp[9]  * srcMatrix[15])
					  - (btmp[0]  * srcMatrix[12] + btmp[7]  * srcMatrix[14] + btmp[8]  * srcMatrix[15]);
	dstMatrix[10] = (btmp[2]  * srcMatrix[12] + btmp[7]  * srcMatrix[13] + btmp[10] * srcMatrix[15])
					  - (btmp[3]  * srcMatrix[12] + btmp[6]  * srcMatrix[13] + btmp[11] * srcMatrix[15]);
	dstMatrix[11] = (btmp[5]  * srcMatrix[12] + btmp[8]  * srcMatrix[13] + btmp[11] * srcMatrix[14])
					  - (btmp[4]  * srcMatrix[12] + btmp[9]  * srcMatrix[13] + btmp[10] * srcMatrix[14]);
	dstMatrix[12] = (btmp[2]  * srcMatrix[10] + btmp[5]  * srcMatrix[11] + btmp[1]  * srcMatrix[9] )
					  - (btmp[4]  * srcMatrix[11] + btmp[0]  * srcMatrix[9]  + btmp[3]  * srcMatrix[10]);
	dstMatrix[13] = (btmp[8]  * srcMatrix[11] + btmp[0]  * srcMatrix[8]  + btmp[7]  * srcMatrix[10])
					  - (btmp[6]  * srcMatrix[10] + btmp[9]  * srcMatrix[11] + btmp[1]  * srcMatrix[8] );
	dstMatrix[14] = (btmp[6]  * srcMatrix[9]  + btmp[11] * srcMatrix[11] + btmp[3]  * srcMatrix[8] )
					  - (btmp[10] * srcMatrix[11] + btmp[2]  * srcMatrix[8]  + btmp[7]  * srcMatrix[9] );
	dstMatrix[15] = (btmp[10] * srcMatrix[10] + btmp[4]  * srcMatrix[8]  + btmp[9]  * srcMatrix[9] )
					  - (btmp[8]  * srcMatrix[9]  + btmp[11] * srcMatrix[10] + btmp[5]  * srcMatrix[8] );

	// calculate determinant
	det = srcMatrix[0] * dstMatrix[0] + srcMatrix[1] * dstMatrix[1] + srcMatrix[2] * dstMatrix[2] + srcMatrix[3] * dstMatrix[3];

	if (det == 0.0) {
		return false;
	}

	// calculate matrix inverse
	invdet = 1.0 / det;
	*(mInv + mInvOffset) = dstMatrix[0]  * invdet;
	*(mInv + 1 + mInvOffset) = dstMatrix[1]  * invdet;
	*(mInv + 2 + mInvOffset) = dstMatrix[2]  * invdet;
	*(mInv + 3 + mInvOffset) = dstMatrix[3]  * invdet;

	*(mInv + 4 + mInvOffset) = dstMatrix[4]  * invdet;
	*(mInv + 5 + mInvOffset) = dstMatrix[5]  * invdet;
	*(mInv + 6 + mInvOffset) = dstMatrix[6]  * invdet;
	*(mInv + 7 + mInvOffset) = dstMatrix[7]  * invdet;

	*(mInv + 8 + mInvOffset) = dstMatrix[8]  * invdet;
	*(mInv + 9 + mInvOffset) = dstMatrix[9]  * invdet;
	*(mInv + 10 + mInvOffset) = dstMatrix[10] * invdet;
	*(mInv + 11 + mInvOffset) = dstMatrix[11] * invdet;

	*(mInv + 12 + mInvOffset) = dstMatrix[12] * invdet;
	*(mInv + 13 + mInvOffset) = dstMatrix[13] * invdet;
	*(mInv + 14 + mInvOffset) = dstMatrix[14] * invdet;
	*(mInv + 15 + mInvOffset) = dstMatrix[15] * invdet;

	return true;
}



