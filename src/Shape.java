public class Shape {
    double refractiveIndex;
    public Shape(Matrix2d points,Matrix2d connectivity){
        // creates shape with given triangles, default refractive index
        refractiveIndex = 1.52F;
    }
    public Shape(Matrix2d points,Matrix2d connectivity,double refractiveIndexIn){
        // create shape with given triangles and refractive index
        refractiveIndex = refractiveIndexIn;
    }
    public Shape(String filename){
        // import shape from mesh file
    }
    public boolean traceLowRes(){
        // Does any ray intersect with this shape?
        // Don't waste time tracing if no rays intersect
        return true; //placeholder
    }
    public double[] traceDistance(Rays rays){
        // find the distance to each shape, for each ray
        return new double[]{0};
    }
    public boolean triangleInterior(Matrix2d points,Matrix2d Q){
        // determines whether query point Q is within the triangle
        // defined by points A,B,C forming each column (in any order)
        // first row is x-axis, second row, is y-axis
        // query is 1 column
        // If query lies on edge, it is considered outside

        // Since many points will be outliers, I optimise by rejecting early.

        // Is the query within the box?
        Matrix2d allPoints = points.concatenateCol(Q); // query column index = 3
        if (allPoints.isMaxCol(0,3)){ // is query maximum x
            return false; //
        }
        if (allPoints.isMinCol(0,3)){ // is query minimum x
            return false;
        }
        if (allPoints.isMaxCol(1,3)){ // is query maximum y
            return false; //
        }
        if (allPoints.isMinCol(1,3)){ // is query minimum y
            return false;
        }

        // Define A as triangle point with minimum x, B as maximum x
        int AIndex = points.minColIndex(0);
        int BIndex = points.maxColIndex(0);
        int CIndex = 0;
        for (int i=0;i<3;i++){
            if (i!=AIndex){
                if (i!=BIndex){
                    CIndex = i;
                }
            }
        }
        Matrix2d A = points.indexCol(AIndex);
        Matrix2d B = points.indexCol(BIndex);
        Matrix2d C = points.indexCol(CIndex);


        // is the signed area of triangle ACB the same polarity as AQB?
        // in other words:
        // does a triangle point upwards or downwards?
        // and,
        // do they both point in the same direction?
        Matrix2d ACB = A.concatenateCol(C.concatenateCol(B));
        Matrix2d AQB = A.concatenateCol(Q.concatenateCol(B));
        double ACBsa = ACB.signedArea(); // calculate only once
        if (ACBsa>0F){ // C points upwards
            if (AQB.signedArea()<=0F){ // Q points downwards or flat
                return false; // Q is outside triangle
            }
        } else if (ACBsa<0F) { // C points downwards
            if (AQB.signedArea()>=0F){ // Q points upwards
                return false; // Q outside triangle
            }
        } else { // ACB signed area == 0
            return false; // triangle is side on, ray skims past
        }

        // The remaining triangle region is bounded by two angles
        // first angle about A
        Matrix2d QAB = Q.concatenateCol(A.concatenateCol(B));
        Matrix2d CAB = C.concatenateCol(A.concatenateCol(B));
        // since each angle is within (0,90deg),
        // comparing the cos of both angles == comparing both angles
        if (QAB.cosTheta() > CAB.cosTheta()){
            return false;
        }
        // second angle about B
        Matrix2d ABQ = A.concatenateCol(B.concatenateCol(Q));
        Matrix2d ABC = A.concatenateCol(B.concatenateCol(C));
        if (ABQ.cosTheta() > ABC.cosTheta()){
            return false;
        }
        return true; // query is bounded by both angles
    }
}
