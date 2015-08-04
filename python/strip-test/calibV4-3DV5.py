import numpy as np
import math
import cv2
import sys
# color conversions from http://www.easyrgb.com/

# according to http://www.easyrgb.com/index.php?X=MATH&H=07
# functions checked with http://www.easyrgb.com/index.php?X=CALC
def RGB2XYZ(R,G,B):
	var_R = R / 255.0        #R from 0 to 255
	var_G = G / 255.0        #G from 0 to 255
	var_B = B / 255.0        #B from 0 to 255

	if var_R > 0.04045:
		var_R = math.pow((var_R + 0.055) / 1.055,2.4)
	else:
		var_R = var_R / 12.92
	
	if var_G > 0.04045:
		var_G = math.pow((var_G + 0.055) / 1.055,2.4)
	else:
		var_G = var_G / 12.92
	
	if var_B > 0.04045:
		var_B = math.pow((var_B + 0.055) / 1.055,2.4)
	else:
		var_B = var_B / 12.92

	var_R = var_R * 100
	var_G = var_G * 100
	var_B = var_B * 100

	#Observer. = 2deg, Illuminant = D65
	X = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805
	Y = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722
	Z = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505

	return X,Y,Z

# functions checked with http://www.easyrgb.com/index.php?X=CALC
def XYZ2LAB(X,Y,Z):
	ref_X =  95.047   #Observer= 2 deg, Illuminant= D65
	ref_Y = 100.000
	ref_Z = 108.883

	var_X = X / ref_X
	var_Y = Y / ref_Y
	var_Z = Z / ref_Z

	if var_X > 0.008856:
		var_X = math.pow(var_X,1.0/3)
	else:
		var_X = 7.787 * var_X + 16.0 / 116
	
	if var_Y > 0.008856:
		var_Y = math.pow(var_Y,1.0/3)
	else:
		var_Y = 7.787 * var_Y + 16.0 / 116
	
	if var_Z > 0.008856:
		var_Z = math.pow(var_Z,1.0/3)
	else:
		var_Z = 7.787 * var_Z + 16.0 / 116

	CIEL = 116.0 * var_Y - 16
	CIEa = 500.0 * (var_X - var_Y)
	CIEb = 200.0 * (var_Y - var_Z)

	return CIEL,CIEa,CIEb

#E94 distance between two lab colours
# checked with http://colormine.org/delta-e-calculator/cie94
def E94Diff(l1,a1,b1,l2,a2,b2):
	dL = l1 - l2
	C1 = math.sqrt(a1 * a1 + b1 * b1)
	C2 = math.sqrt(a2 * a2 + b2 * b2)
	dC = C1 - C2  
	da = a1 - a2 
	db = b1 - b2
	
	dE = math.sqrt(dL**2 + da**2 + db**2)
	if math.sqrt(dE) > (math.sqrt(abs(dL)) + math.sqrt(abs(dC))):
		dH = math.sqrt((dE * dE) - (dL * dL) - (dC * dC))
	else:
		dH = 0.0

	SL = 1.0
	SC = 1.0 + 0.045 * C1   
	SH = 1.0 + 0.015 * C1

	dE94 = math.sqrt((dL/SL)**2 + (dC/SC)**2 + (dH/SH)**2)

	return dE94

# calibration values of QPcard 201
# numbering, with text Qpcard placed on the left:
# text | 0  1  2  3  4  5  6  7  8  9
# text | 10 11 12 13 14 15 16 17 18 19
# text | 20 21 22 23 24 25 26 27 28 29
# order B,G,R
# greyscale, from light to dark: 8,7,9,6,5,4,3
def createQPcardCalTable():
	qpCardCal = np.zeros(shape=(30,3))

	qpCardCal[0] =[184,186,186]
	qpCardCal[1] =[142,89,66]
	qpCardCal[2] =[64,201,230]
	qpCardCal[3] =[64,64,64]
	qpCardCal[4] =[94,94,94]
	qpCardCal[5] =[117,116,115]
	qpCardCal[6] =[157,159,159]
	qpCardCal[7] =[212,214,214]
	qpCardCal[8] =[238,241,239]
	qpCardCal[9] =[184,186,186]
	qpCardCal[10] =[46,180,179]
	qpCardCal[11] =[109,161,92]
	qpCardCal[12] =[122,81,171]
	qpCardCal[13] =[80,66,76]
	qpCardCal[14] =[69,104,93]
	qpCardCal[15] =[62,121,136]
	qpCardCal[16] =[71,83,105]
	qpCardCal[17] =[128,148,177]
	qpCardCal[18] =[216,219,203]
	qpCardCal[19] =[212,193,202]
	qpCardCal[20] =[184,186,186]
	qpCardCal[21] =[57,50,130]
	qpCardCal[22] =[189,149,81]
	qpCardCal[23] =[69,61,101]
	qpCardCal[24] =[157,126,108]
	qpCardCal[25] =[49,112,209]
	qpCardCal[26] =[37,166,233]
	qpCardCal[27] =[170,207,234]
	qpCardCal[28] =[193,219,217]
	qpCardCal[29] =[184,186,186]

	return qpCardCal

# Qpcard values for grayscale
# from qpcard201.it8.txt
def grayCalVal(qpCardCal):
	Bcal = [qpCardCal[8,0],qpCardCal[7,0],qpCardCal[9,0],qpCardCal[6,0],qpCardCal[5,0],qpCardCal[4,0],qpCardCal[3,0]]
	Gcal = [qpCardCal[8,1],qpCardCal[7,1],qpCardCal[9,1],qpCardCal[6,1],qpCardCal[5,1],qpCardCal[4,1],qpCardCal[3,1]] 
	Rcal = [qpCardCal[8,2],qpCardCal[7,2],qpCardCal[9,2],qpCardCal[6,2],qpCardCal[5,2],qpCardCal[4,2],qpCardCal[3,2]]

	return Bcal,Gcal,Rcal

def grayMeasVal(qpCard):
	Bmeas = [qpCard[8,0],qpCard[7,0],qpCard[9,0],qpCard[6,0],qpCard[5,0],qpCard[4,0],qpCard[3,0]]
	Gmeas = [qpCard[8,1],qpCard[7,1],qpCard[9,1],qpCard[6,1],qpCard[5,1],qpCard[4,1],qpCard[3,1]] 
	Rmeas = [qpCard[8,2],qpCard[7,2],qpCard[9,2],qpCard[6,2],qpCard[5,2],qpCard[4,2],qpCard[3,2]]

	return Bmeas,Gmeas,Rmeas

def dist2(b1,g1,r1,b2,g2,r2):
	return (b1-b2)**2 + (g1-g2)**2 + (r1-r2)**2

def measureSquareL(img,bitDepth,x,y,d):
	totalL = 0
	totalNum = 0
	for j in range (-d,d):
		for jj in range(-d,d):
			totalL = totalL + img[y+j,x+jj,1]
			totalNum = totalNum + 1

	avgL = 1.0 * totalL / totalNum
	return avgL

def measureSquare(img,bitDepth,x,y,d):
	totalB = totalG = totalR = 0
	totalNum = 0
	if bitDepth == 8:
		fac = 1.0
	else: 
		fac = 1.0 / 256

	for j in range (-d,d):
		for jj in range(-d,d):
			totalB = totalB + img[y+j,x+jj,0] * fac
			totalG = totalG + img[y+j,x+jj,1] * fac
			totalR = totalR + img[y+j,x+jj,2] * fac
			totalNum = totalNum + 1

	avgB = 1.0 * totalB / totalNum
	avgG = 1.0 * totalG / totalNum
	avgR = 1.0 * totalR / totalNum

	totalB = totalG = totalR = 0
	totalNum = 0
	for j in range (-d,d):
		for jj in range(-d,d):
			if dist2(img[y+j,x+jj,0] * fac,img[y+j,x+jj,1] * fac,img[y+j,x+jj,2] * fac,avgB,avgG,avgR) < 100: 
				totalB = totalB + img[y+j,x+jj,0] * fac
				totalG = totalG + img[y+j,x+jj,1] * fac
				totalR = totalR + img[y+j,x+jj,2] * fac
				totalNum = totalNum + 1

	avgB = 1.0 * totalB / totalNum
	avgG = 1.0 * totalG / totalNum
	avgR = 1.0 * totalR / totalNum

	return avgB, avgG, avgR

# xoff, yoff: offset of first block
# dist: distance between blocks
# size: size of block
# card numbering: column, row, with column=0....9, row=0...2
def measureCard(img,bitDepth,xoff,yoff,dist,size):
	qpCard = np.zeros(shape=(30,3))

	# get values
	for i in range(0,3):
		for ii in range(0,10):
			sqNum = 29 - 10 * i - ii
			B,G,R = measureSquare(img, bitDepth, int(xoff + ii * (dist + size) + size * 0.5), int(yoff + i * (dist + size) + size * 0.5),15)
			qpCard[sqNum,0] = B
			qpCard[sqNum,1] = G
			qpCard[sqNum,2] = R
	return qpCard

def measureCardIllumHLS(img,xoff,yoff,dist,size,d):
	qpCardIllum = np.zeros(shape=(40,3))

	# get values
	num = 0
	corrY = 0
	for i in range(0,4): # rows
		for ii in range(0,10): # cols
			if i == 3:
				corrY = -dist/4;
			x = int(xoff + ii * (dist + size))
			y = int(yoff + i * (dist + size) + corrY)
			L = measureSquareL(img, 8, x, y,d) 
			# print "L", L
			qpCardIllum[num,0] = x
			qpCardIllum[num,1] = y
			qpCardIllum[num,2] = L
			num = num + 1
			# print x,y,CIE_L 
	return qpCardIllum

def addBlackPixel(img,x,y):
	img[y,x,0] = 0;
	img[y,x,1] = 0;
	img[y,x,2] = 0;

# visual check illumination pionts
def addBlack(img,bitDepth,xoff,yoff,dist,size,d):
	imgCopy = np.copy(img)
	corrY = 0
	for i in range(0,4):
		for ii in range(0,10):
			if i == 3:
				corrY = -dist/2;
			sqNum = 29 - 10 * i - ii
			for j in range (-d,d):
				for jj in range(-d,d):
					addBlackPixel(imgCopy,int(xoff + ii * (dist + size) + j), int(yoff + i * (dist + size) + corrY + jj))

	bgr = cv2.cvtColor(imgCopy, cv2.COLOR_HLS2BGR_FULL)
	# cv2.imshow('image',bgr)
	# cv2.waitKey(0)


def addColour(img,bitdepth,x,y,calVals):
	if bitdepth == 16:
		fac = 256
	else:
		fac = 1
	img[y,x,0] = calVals[0] * fac;
	img[y,x,1] = calVals[1] * fac;
	img[y,x,2] = calVals[2] * fac;

# visual check, puts calibrated values in sample area
def addCalColours(img,bitDepth,xoff,yoff,dist,size,calTable,filename):
	d = 15
	imgCopy = np.copy(img)
	for i in range(0,3):
		for ii in range(0,10):
			sqNum = 29 - 10 * i - ii
			for j in range (-d,d):
				for jj in range(-d,d):
					addColour(imgCopy,bitDepth,int(xoff + ii * (dist + size) + size * 0.5 + j), int(yoff + i * (dist + size) + size * 0.5 + jj),calTable[sqNum])

	# cv2.imshow('image',imgCopy)
	cv2.imwrite(filename,imgCopy)

#create a numpy array of 256 int (= single channel of the lookup table)
def createLUT(pol2B,pol2G,pol2R):
	lutB = np.arange(256, dtype = "uint16")
	lutG = np.arange(256, dtype = "uint16")
	lutR = np.arange(256, dtype = "uint16")

	for i in range(0,256):
		detB = pol2B[1]**2 - 4.0 * pol2B[0] * (pol2B[2]-i)
		if detB < 0:
			lutB[i] = i * 256
		else:
			B = int(round((-pol2B[1] + math.sqrt(detB)) * 256 / (2 * pol2B[0])))
			if B < 0:
				lutB[i] = 0
			else:
				if B > 65535:
					lutB[i] = 65535
				else:
					lutB[i] = B
		
		detG = pol2G[1]**2 - 4.0 * pol2G[0] * (pol2G[2]-i)
		if detG < 0:
			lutG[i] = i * 256
		else:
			G = int(round((-pol2G[1] + math.sqrt(detG)) * 256 / (2 * pol2G[0])))
			if G < 0:
				lutG[i] = 0
			else:
				if G > 65535:
					lutG[i] = 65535
				else:
					lutG[i] = G

		detR = pol2R[1]**2 - 4.0 * pol2R[0] * (pol2R[2]-i)
		if detR < 0:
			lutR[i] = i * 256
		else:
			R = int(round((-pol2R[1] + math.sqrt(detR)) * 256 / (2.0 * pol2R[0])))
			if R < 0:
				lutR[i] = 0
			else:
				if R > 65535:
					lutR[i] = 65535
				else:
					lutR[i] = R

	return cv2.merge((lutB,lutG,lutR))

# compute colour differences between original measured and calibration values
# qpCardCal has the real calibration card values
# qpCard1 has had illumination homogeneity correction
# qpCard2 has had gray scale calibration (gamma + white balance)
# qpCard3 has had 3D LUT calibration
def computeErrors(qpCardCal,qpCard1,qpCard2,qpCard3):
	E94_1_tot = E94_2_tot = E94_3_tot = 0
	E94_1_max = E94_2_max = E94_3_max = -100

	for i in range(0,30):
		X_cal,Y_cal,Z_cal = RGB2XYZ(qpCardCal[i,2],qpCardCal[i,1],qpCardCal[i,0])
		CIE_L_cal,CIE_a_cal,CIE_b_cal = XYZ2LAB(X_cal,Y_cal,Z_cal)

		X1,Y1,Z1 = RGB2XYZ(qpCard1[i,2],qpCard1[i,1],qpCard1[i,0])
		CIE_L1,CIE_a1,CIE_b1 = XYZ2LAB(X1,Y1,Z1)
		E94_1 = E94Diff(CIE_L_cal,CIE_a_cal,CIE_b_cal,CIE_L1,CIE_a1,CIE_b1)
		E94_1_tot = E94_1_tot + E94_1
		if (E94_1 > E94_1_max):
			E94_1_max = E94_1

		X2,Y2,Z2 = RGB2XYZ(qpCard2[i,2],qpCard2[i,1],qpCard2[i,0])
		CIE_L2,CIE_a2,CIE_b2 = XYZ2LAB(X2,Y2,Z2)
		E94_2 = E94Diff(CIE_L_cal,CIE_a_cal,CIE_b_cal,CIE_L2,CIE_a2,CIE_b2)
		E94_2_tot = E94_2_tot + E94_2
		if (E94_2 > E94_2_max):
			E94_2_max = E94_2

		X3,Y3,Z3 = RGB2XYZ(qpCard3[i,2],qpCard3[i,1],qpCard3[i,0])
		CIE_L3,CIE_a3,CIE_b3 = XYZ2LAB(X3,Y3,Z3)
		E94_3 = E94Diff(CIE_L_cal,CIE_a_cal,CIE_b_cal,CIE_L3,CIE_a3,CIE_b3)
		E94_3_tot = E94_3_tot + E94_3
		if (E94_3 > E94_3_max):
			E94_3_max = E94_3

	print "############# Errors ################\n"
	print "illum mean error: ", E94_1_tot/30, ", max error: ", E94_1_max
	print "gray scale mean error: ", E94_2_tot/30, ", max error: ", E94_2_max
	print "3D LUT mean error:",E94_3_tot/30, ", max error: ", E94_3_max

############################### start of main programme #############################
# input 1
xoff=55
yoff=38
dist=43
size=148

xoffb=30
yoffb=21
distb=43
sizeb=148

# input 2
# xoff=27
# yoff=84
# dist=57
# size=180

# Load an color image in grayscale
img = cv2.imread('input1.jpg',cv2.IMREAD_COLOR)

# all Qp card calibration values.
qpCardCal = createQPcardCalTable()

# create image with superimposed calibration values
addCalColours(img,8,xoff,yoff,dist,size,qpCardCal,"new-nocal.png")

####################### start illumination homogeneity correction ####################
# Using https://clouard.users.greyc.fr/Pantheon/experiments/illumination-correction/index-en.html
hls = cv2.cvtColor(img, cv2.COLOR_BGR2HLS_FULL)

xyl = measureCardIllumHLS(hls,xoffb,yoffb,distb,sizeb,3)

# model this with a linear slope with lstsq fit
# http://stackoverflow.com/questions/15959411/fit-points-to-a-plane-algorithms-how-to-iterpret-results
# The plane is: L = aX + bY + c

[rows,cols] = xyl.shape
# the third column is for the constant c
Col = np.ones((rows,3))
Col[:,0] = xyl[:,0]  #X
Col[:,1] = xyl[:,1]  #Y
L = xyl[:,2]
(a,b,c),resid,rank,s = np.linalg.lstsq(Col,L) 
print "plane model (z = ax + by + c): ", a,b,c

#compute mean (the value of the plane in the middle of the image)
[ver,hor,depth] = img.shape
Lmean = a * (hor/2) + b * (ver/2) + c 
print "dimensions:",hor, ver
print "Lmean:",Lmean

# correct image
# use the the plane to recalibrate the patches
imgIllCorr = np.zeros((hls.shape[0],hls.shape[1],3), np.uint8)
for i in range(0,hls.shape[0]): # rows (y)
	for ii in range(0,hls.shape[1]): # cols (x)
		imgIllCorr[i,ii,0] = hls[i,ii,0]
		imgIllCorr[i,ii,1] = int(round(hls[i,ii,1] - (a * ii + b * i + c) + Lmean))
		imgIllCorr[i,ii,2] = hls[i,ii,2]
		
imgCorrRGB = cv2.cvtColor(imgIllCorr, cv2.COLOR_HLS2BGR_FULL)

# export result with calibration values
addCalColours(imgCorrRGB,8,xoff,yoff,dist,size,qpCardCal,"new-illum-homog.png")

######################## end illumination correction ######################

######################## start gray callibration - 1D LUT #################
qpCard1 = measureCard(imgCorrRGB,8,xoff,yoff,dist,size)

# start grayscale calibration
Bcal,Gcal,Rcal = grayCalVal(qpCardCal)
Bmeas,Gmeas,Rmeas = grayMeasVal(qpCard1)

pol2B = np.polyfit(Bcal,Bmeas,2)
pol2G = np.polyfit(Gcal,Gmeas,2)
pol2R = np.polyfit(Rcal,Rmeas,2)

# print pol2B,pol2G,pol2R

# create 1D linearizing lut. This does gamma correction and white balance
lut = createLUT(pol2B,pol2G,pol2R)

# apply LUT and save result
img1Dcorr = np.zeros((imgCorrRGB.shape[0],imgCorrRGB.shape[1],3), np.uint16)
cv2.LUT(imgCorrRGB,lut,img1Dcorr)

# export result with calibration values attached.
addCalColours(img1Dcorr,16,xoff,yoff,dist,size,qpCardCal,"new-1D-corr.png")
qpCard2 = measureCard(img1Dcorr,16,xoff,yoff,dist,size)
######################## end gray callibration - 1D LUT #################


############################## start 3D LUT correction ##############################
#Following http://docs.scipy.org/doc/scipy/reference/tutorial/linalg.html#solving-linear-least-squares-problems-and-pseudo-inverses
# we will solve P = M x 

# first create matrix M:
#     (B1 G1 R1)
#     (B2 G2 R2)
# M = (B3 G3 R3)
#     (B4 G4 R4)
#		....
#     (Bn Gn Rn)

M1 = []
Cal1 = []
for i in range(0,30):
	M1.append([qpCard2[i,0],qpCard2[i,1],qpCard2[i,2]])
	Cal1.append([qpCardCal[i,0],qpCardCal[i,1],qpCardCal[i,2]])

M = np.array(M1)
Cal = np.array(Cal1)

sol,resid,rank,s = np.linalg.lstsq(M,Cal)

# use these to recalibrate the patches
img3Dcorr = np.zeros((img.shape[0],img.shape[1],3), np.uint16)
for i in range(0,img.shape[0]):
	for ii in range(0,img.shape[1]):
		Bcorr = int(round(sol[0,0] * img1Dcorr[i,ii,0] + sol[1,0] * img1Dcorr[i,ii,1]+ sol[2,0] * img1Dcorr[i,ii,2]))
		if Bcorr>65535:
			Bcorr = 65535
		if Bcorr<0:
			Bcorr = 0
		
		Gcorr = int(round(sol[0,1] * img1Dcorr[i,ii,0] + sol[1,1] * img1Dcorr[i,ii,1]+ sol[2,1] * img1Dcorr[i,ii,2])) 
		if Gcorr>65535:
			Gcorr = 65535
		if Gcorr<0:
			Gcorr = 0
		
		Rcorr = int(round(sol[0,2] * img1Dcorr[i,ii,0] + sol[1,2] * img1Dcorr[i,ii,1]+ sol[2,2] * img1Dcorr[i,ii,2]))
		if Rcorr>65535:
			Rcorr = 65535
		if Rcorr<0:
			Rcorr = 0
		
		img3Dcorr[i,ii] = [Bcorr,Gcorr,Rcorr]

# export result with calibration colours added
addCalColours(img3Dcorr,16,xoff,yoff,dist,size,qpCardCal,"new-3Dcal-corr.png")
qpCard3 = measureCard(img3Dcorr,16,xoff,yoff,dist,size)

############################## end 3D LUT correction ##############################

computeErrors(qpCardCal, qpCard1, qpCard2, qpCard3)

cv2.destroyAllWindows()