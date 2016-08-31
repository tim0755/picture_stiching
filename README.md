# picture_stiching
stiching two pictures which have a overlay block.  

the two input pictures have the same height and width.  


support correctionMode:  

0: first, get the coefficients of two picture,  

1: all pixel adjust by coefficients for picture A, all pixel except overlay adjust by coefficients' for picture B  

2: first,all pixel adjust by coefficients for picture A; all pixel adjust by coefficients' for picture B. then, for the overlay, blend pixel from A and pixel from B by weight  

3: first, all pixel adjust by coefficients by weight for picture A; all pixel adjust by coefficients' by weight for picture B. then, for the overlay , blend pixel from A and pixel from B by weight  

4: base on mode 3 above, consider the picture A and picture B have two overlay block. the left of A overlay to right of B; the right of A overlay to left of B.  


拼接模式：  

0: 得到各自的校正系数后,  

1: 图片A完全用系数校正；图片B除了重叠区外，用系数校正.  

2: 图片A和图片B用各自的系数校正后，重叠区按照权值进行混合.  

3: 图片A和图片B用系数校正时，进行渐变；然后重叠区使用渐变后的值加权混合.  

4: 在第3种方法的基础上，考虑图片A和图片B有两个重叠区：A左和B右重叠，A右和B左重叠:  

    a. 图片A和B的中间保持颜色值不变  

    b. 然后从中间往两边渐变，到最边缘渐变为Ca*Pa和Cb*Pb。其中Cn表示n图片某点的颜色值，Pn表示n图片的校正系数。Pa*Pb==1  

    c. 两个图片的重叠区取两个图片的加权平均值，其中权值取该点在重叠区内的偏移量，该点靠近A，则A的权值更大。两个权值的和为1  

    d. 对于双目的来说，A图的左边和B图的右边重叠，A图的右边和B图的左边重叠。需要计算两个重叠区  

