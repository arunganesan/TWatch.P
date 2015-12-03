package edu.umich.eecs.twatchp;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Arun on 11/22/2014.
 */
public class AutoTuner {
    MainActivity mainActivity;
    CountdownBuffer buffer;
    Recorder recorder;
    Player player;

    private final static String TAG = "AutoTuner";
    public double [] shortchirp = {1,0.9596,0.84139,0.65457,0.41401,0.13912,-0.14768,-0.4228,-0.6634,-0.84932,-0.96477,-0.99971,-0.95078,-0.82167,-0.62287,-0.37088,-0.086886,0.20502,0.47985,0.71386,0.88664,0.9829,0.99386,0.91814,0.76192,0.5385,0.26723,-0.028128,-0.32149,-0.5867,-0.79993,-0.94179,-0.99913,-0.96637,-0.846,-0.64853,-0.39158,-0.09837,0.20436,0.48875,0.72843,0.90093,0.98987,0.98651,0.89071,0.71103,0.46398,0.17263,-0.13559,-0.43139,-0.68643,-0.87606,-0.9817,-0.99275,-0.90767,-0.73426,-0.48899,-0.19551,0.11763,0.41968,0.68076,0.8748,0.98219,0.99179,0.90218,0.72187,0.46859,0.16757,-0.1509,-0.45453,-0.71226,-0.89752,-0.99093,-0.98244,-0.87245,-0.67187,-0.40113,-0.088141,0.23455,0.53312,0.77602,0.93733,0.99958,0.9557,0.80985,0.57718,0.28223,-0.043529,-0.36509,-0.64759,-0.86019,-0.97938,-0.99171,-0.89534,-0.70037,-0.42786,-0.10759,0.22509,0.5332,0.78224,0.94403,1,0.94337,0.78002,0.528,0.21557,-0.12191,-0.44593,-0.71926,-0.9102,-0.99635,-0.96726,-0.82579,-0.58793,-0.28104,0.059225,0.39302,0.68097,0.88883,0.99159,0.97657,0.84504,0.61224,0.30564,-0.03817,-0.37786,-0.6723,-0.88554,-0.99126,-0.97609,-0.84137,-0.60312,-0.29024,0.058935,0.40129,0.69427,0.90114,0.99569,0.96558,0.81406,0.55972,0.23427,-0.12133,-0.46199,-0.74422,-0.93168,-0.99993,-0.93969,-0.75819,-0.47839,-0.13616,0.22424,0.55588,0.81528,0.96809,0.99379,0.88844,0.66544,0.35389,-0.0051611,-0.364,-0.67464,-0.89521,-0.99568,-0.96201,-0.79818,-0.5259,-0.18174,0.18766,0.53189,0.80365,0.96527,0.99403,0.88541,0.65391,0.33121,-0.038073,-0.40252,-0.71104,-0.92005,-0.99969,-0.9382,-0.74373,-0.44336,-0.079449,0.29628,0.63006,0.87381,0.99208,0.96729,0.80248,0.52099,0.16333,-0.21861,-0.56907,-0.8365,-0.98126,-0.98151,-0.83666,-0.5676,-0.21385,0.17222,0.53304,0.81447,0.97393,0.98693,0.85095,0.58598,0.23176,-0.15818,-0.52447,-0.811,-0.97352,-0.98656,-0.84755,-0.57738,-0.21742,0.17677,0.5439,0.82657,0.98019,0.98016,0.82591,0.54119,0.17053,-0.22762,-0.59007,-0.85893,-0.99089,-0.9643,-0.78283,-0.47512,-0.090362,0.30948,0.65961,0.9029,0.99929,0.93248,0.71282,0.37581,-0.023477,-0.41937,-0.74633,-0.94988,-0.99569,-0.87556,-0.60895,-0.24,0.1697,0.55131,0.84031,0.98747,0.96729,0.78262,0.46433,0.066319,-0.34351,-0.69484,-0.92697,-0.99942,-0.89908,-0.64273,-0.27437,0.14223,0.53446,0.83357,0.98674,0.96648,0.77576,0.44768,0.039897,-0.37544,-0.7244,-0.94447,-0.99582,-0.86863,-0.58516,-0.19592,0.22915,0.61321,0.88642,0.99868,0.92898,0.6894,0.32324,-0.10268,-0.51018,-0.824,-0.98579,-0.96502,-0.76491,-0.42212,-0.00015346,0.42231,0.76604,0.96615,0.98442,0.81679,0.49435,0.077758,-0.35408,-0.7189,-0.94676,-0.99356,-0.8497,-0.54221,-0.12982,0.3081,0.68682,0.93259,0.99714,0.86726,0.5677,0.15657,-0.28575,-0.6723,-0.92663,-0.99801,-0.87168,-0.57206,-0.15824,0.28759,0.67639,0.93004,0.99712,0.86352,0.55553,0.13487,-0.31358,-0.6988,-0.94215,-0.99351,-0.84174,-0.51727,-0.08621,0.36305,0.73788,0.96051,0.9843,0.80367,0.45555,0.012026,-0.43447,-0.79039,-0.98069,-0.96478,-0.74534,-0.36812,0.087482,0.52498,0.85122,0.99627,0.92856,0.66189,0.25288,-0.21095,-0.62977,-0.91297,-0.99878,-0.86796,-0.54827,-0.10878,0.35491,0.7414,0.96567,0.97791,0.77476,0.40038,-0.06295,-0.51279,-0.84915,-0.99681,-0.92221,-0.64134,-0.2165,0.25738,0.67382,0.93873,0.99179,0.82032,0.46252,-0.00079961,-0.4644,-0.82241,-0.99257,-0.93529,-0.66302,-0.2378,0.24271,0.66756,0.93814,0.99114,0.81355,0.44609,-0.025964,-0.4924,-0.84375,-0.99706,-0.91561,-0.6179,-0.17373,0.31206,0.72419,0.96432,0.97466,0.75204,0.34908,-0.13805,-0.59244,-0.9045,-0.99843,-0.85086,-0.49684,-0.021765,0.45905,0.82834,0.9955,0.91897,0.61682,0.16269,-0.33196,-0.74511,-0.97429,-0.96213,-0.71095,-0.28256,0.21671,0.66228,0.94242,0.98634,0.78232,0.38102,-0.11684,-0.5856,-0.90639,-0.99731,-0.83455,-0.45887,0.034376,0.51928,0.87145,1,0.87119,0.51751,0.029727,-0.4662,-0.8416,-0.9985,-0.8954,-0.55847,-0.075165,0.42828,0.81972,0.99586,0.90965,0.5831,0.10195,-0.40666,-0.8077,-0.99411,-0.91557,-0.59231,-0.11019,0.40192,0.80647,0.99422,0.91391,0.58648,0.099929,-0.41417,-0.81613,-0.99613,-0.90444,-0.56537,-0.071105,0.44311,0.83592,0.99876,0.88604,0.52817,0.023623,-0.48792,-0.8642,-0.99997,-0.85668,-0.47362,0.042512,0.54715,0.89831,0.99659,0.81355,0.4002,-0.12694,-0.61849,-0.93451,-0.98444,-0.7533,-0.30646,0.22861,0.69849,0.96783,0.95844,0.67229,0.1914,-0.34537,-0.78231,-0.99205,-0.91285,-0.56704,-0.055081,0.47346,0.86344,0.99979,0.84164,0.43489,-0.10081,-0.6071,-0.93361,-0.98275,-0.73915,-0.27474,0.27221,0.73807,0.98284,0.93232,0.60096,0.087997,-0.45199,-0.85556,-0.9999,-0.84048,-0.42502,0.12042,0.62951,0.94632,0.97319,0.7011,0.21301,-0.34115,-0.79039,-0.99544,-0.8921,-0.51165,0.028273,0.55982,0.91704,0.98774,0.74905,0.27507,-0.28578,-0.75707,-0.98987,-0.90997,-0.54188,-0.0016073,0.53961,0.9095,0.98966,0.75376,0.27646,-0.28983,-0.76352,-0.99196,-0.90094,-0.51901,0.031051,0.57149,0.92692,0.98131,0.71621,0.21727,-0.35305,-0.80827,-0.99881,-0.86144,-0.44052,0.12593,0.65124,0.96158,0.95358,0.62911,0.095235,-0.47077,-0.88017,-0.99577,-0.77815,-0.29944,0.28022,0.76604,0.99407,0.88667,0.47934,-0.090683,-0.63037,-0.95621,-0.95673,-0.63095,-0.089364,0.48322,0.8906,0.99248,0.75308,0.25404,-0.33306,-0.80546,-0.99932,-0.8467,-0.3998,0.18658,0.70836,0.98319,0.91434,0.52509,-0.048725,-0.60584,-0.95006,-0.95945,-0.6299,-0.077194,0.50328,0.90551,0.98601,0.71533,0.18922,-0.40492,-0.85454,-0.99807,-0.78323,-0.28646,0.3139,0.80141,0.99954,0.83585,0.36886,-0.23249,-0.74968,-0.99398,-0.87556,-0.43686,0.16218,0.70216,0.98449,0.90466,0.49123,-0.1039,-0.66103,-0.97367,-0.92523,-0.53286,0.058161,0.62793,0.96358,0.93902,0.56261,-0.025221,-0.60399,-0.95578,-0.94739,-0.58117,0.0051772,0.58998,0.9513,0.95126,0.58904,0.0019465,-0.58631,-0.95069,-0.95106,-0.58644,0.0038527,0.59308,0.95404,0.94679,0.5733,-0.022573,-0.6101,-0.96092,-0.93793,-0.54925,0.054193,0.63686,0.97046,0.92355,0.51369,-0.098628,-0.67249,-0.98126,-0.90223,-0.46583,0.15564,0.71571,0.9914,0.87219,0.40478,-0.22476,-0.76474,-0.99845,-0.83133,-0.3297,0.30509,0.81722,0.99946,0.77732,0.23995,-0.3952,-0.8701,-0.991,-0.70781,-0.1353,0.49295,0.91965,0.96926,0.62056,0.016193,-0.59525,-0.96135,-0.93018,-0.51379,0.11599,0.69801,0.99003,0.86976,0.38648,-0.25864,-0.79594,-0.99996,-0.78432,-0.23877,0.40768,0.88263,0.98513,0.671,0.07241,-0.55729,-0.95062,-0.93969,-0.52829,0.10886,0.6999,0.99171,0.85854,0.35666,-0.29891,-0.82619,-0.99749,-0.73806,-0.1592,0.48901,0.92548,0.96012,0.577,-0.057822,-0.66782,-0.98632,-0.8733,-0.37738,0.28453,0.82177,0.99753,0.73355,0.14546,-0.50734,-0.93586,-0.94955,-0.54147,0.10774,0.70937,0.9949,0.8361,0.30309,-0.36608,-0.87139,-0.98542,-0.65602,-0.030744,0.60881,0.97357,0.89786,0.41507,-0.25646,-0.81181,-0.9979,-0.72911,-0.12723,0.5332,0.94988,0.93122,0.48489,-0.18473,-0.76968,-1,-0.76858,-0.18152,0.48999,0.93463,0.94537,0.51635,-0.15349,-0.75213,-0.99976,-0.77981,-0.19437,0.48262,0.93328,0.94509,0.51161,-0.16354,-0.76182,-1,-0.76447,-0.16605,0.51164,0.94628,0.93026,0.47033,-0.21464,-0.79731,-0.99826,-0.72029,-0.096023,0.57483,0.96908,0.89593,0.38978,-0.30529,-0.85284,-0.98686,-0.64142,0.016355,0.66655,0.99188,0.83273,0.26602,-0.43139,-0.91753,-0.95314,-0.51979,0.16992,0.77621,0.99934,0.72819,0.096103,-0.58403,-0.97442,-0.88029,-0.34758,0.35884,0.88642,0.97091,0.56913,-0.11835,-0.74679,-1,-0.74972,-0.1212,0.56889,0.9717,0.88269,0.34605,-0.36662,-0.89329,-0.96546,-0.54546,0.15325,0.77402,0.99893,0.71194,0.059354,-0.62411,-0.98676,-0.84109,-0.2613,0.45385,0.93457,0.93134,0.4449,-0.27294,-0.84921,-0.98344,-0.60479,0.089959,0.73805,1,0.7377,0.0879,-0.60849,-0.98491,-0.84231,-0.25499,0.46744,0.94287,0.91886,0.4072,-0.32112,-0.87899,-0.96885,-0.54188,0.17479,0.79837,0.99468,0.65764,-0.032746,-0.70591,-0.99937,-0.75419,-0.10175,0.60606,0.98623,0.83211,0.22635,-0.50274,-0.95869,-0.89261,-0.33957,0.39925,0.92012,0.93738,0.44065,-0.2983,-0.87365,-0.96839,-0.52941,0.20198,0.82216,0.98774,0.60616,-0.1119,-0.76819,-0.99758,-0.67154,0.029145,0.71393,0.99996,0.72639,0.045546,-0.66123,-0.99682,-0.77174,-0.11177,0.6116,0.98992,0.80862,0.16935,-0.56627,-0.98082,-0.83809,-0.2183,0.52622,0.97088,0.86112,0.25872,-0.49217,-0.96122,-0.87858,-0.29079,0.4647,0.95275,0.89123,0.3147,-0.4442,-0.94618,-0.89963,-0.33062,0.43093,0.942,0.90422,0.33867,-0.42508,-0.94051,-0.90521,-0.33893,0.4267,0.94181,0.90268,0.3314,-0.43578,-0.94582,-0.89648,-0.316,0.45221,0.95225,0.8863,0.29263,-0.47578,-0.96061,-0.87164,-0.26111,0.50616,0.97022,0.85186,0.22125,-0.54289,-0.98018,-0.82616,-0.17287,0.58533,0.98937,0.7936,0.11586,-0.63265,-0.99646,-0.75318,-0.050209,0.68378,0.9999,0.70384,-0.023929,-0.73737,-0.99795,-0.64454,0.10616,0.79175,0.98867,0.57433,-0.19579,-0.84493,-0.97001,-0.49243,0.29174,0.89457,0.93984,0.39833,-0.39244,-0.93799,-0.89605,-0.29194,0.49584,0.97218,0.83665,0.17365,-0.59927,-0.99389,-0.75994,-0.044529,0.69946,0.99972,0.66465,-0.093579,-0.79255,-0.98626,-0.55016,0.23794,0.87409,0.9503,0.41671,-0.38485,-0.93925,-0.88904,-0.2656,0.52959,0.9829,0.80045,0.099383,-0.66647,-0.99993,-0.68351,0.077919,0.78894,0.98561,0.53863,-0.26077,-0.88982,-0.93596,-0.36794,0.44207,0.96169,0.84829,0.17559,-0.61332,-0.99732,-0.7217,0.032052,0.7649,0.99029,0.5576,-0.24633,-0.88653,-0.93575,-0.36018,0.45644,0.96796,0.83114,0.13672,-0.64983,-0.99983,-0.67698,0.10224,0.81284,0.97472,0.47755,-0.34315,-0.93165,-0.88824,-0.24141,0.5699,0.99354,0.74012,-0.018503,-0.76483,-0.98835,-0.53518,0.28509,0.91006};
    public double [] longchirp = {1, 0.95964, 0.84167, 0.65545, 0.41589, 0.14232, -0.14307, -0.41705, -0.65719, -0.84375, -0.96129, -0.99996, -0.95638, -0.83392, -0.64248, -0.39773, -0.11979, 0.16833, 0.4427, 0.68043, 0.86156, 0.97081, 0.99883, 0.94307, 0.80797, 0.60472, 0.35029, 0.066029, -0.22406, -0.4954, -0.72489, -0.89288, -0.9849, -0.99289, -0.91593, -0.76042, -0.53949, -0.27198, 0.019198, 0.30896, 0.57226, 0.78623, 0.93217, 0.99723, 0.97553, 0.86874, 0.68597, 0.44302, 0.16103, -0.13533, -0.42004, -0.66798, -0.85718, -0.97074, -0.99842, -0.93754, -0.79329, -0.5783, -0.31156, -0.016759, 0.27979, 0.5515, 0.77392, 0.92692, 0.99653, 0.97627, 0.86773, 0.68052, 0.43143, 0.14293, -0.15882, -0.44634, -0.69333, -0.8771, -0.98065, -0.99427, -0.91648, -0.75422, -0.52224, -0.24182, 0.061225, 0.35884, 0.62336, 0.83011, 0.95968, 0.99976, 0.9464, 0.80436, 0.58674, 0.31381, 0.011122, -0.29286, -0.56944, -0.79237, -0.9404, -0.99929, -0.96323, -0.83542, -0.62781, -0.36006, -0.057629, 0.25057, 0.53495, 0.76812, 0.92747, 0.99746, 0.9711, 0.85071, 0.64773, 0.38167, 0.078273, -0.233, -0.52177, -0.75977, -0.92355, -0.99688, -0.97233, -0.85208, -0.64774, -0.3793, -0.073135, 0.24051, 0.53055, 0.7681, 0.92938, 0.99814, 0.96729, 0.83968, 0.62785, 0.35286, 0.042189, -0.27297, -0.56086, -0.79233, -0.94384, -0.99986, -0.95446, -0.81202, -0.58681, -0.30159, 0.014643, 0.32963, 0.61109, 0.83006, 0.96389, 0.99861, 0.93039, 0.76604, 0.52234, 0.22433, -0.097181, -0.40884, -0.67819, -0.87704, -0.98446, -0.98901, -0.88997, -0.69748, -0.43156, -0.12001, 0.20442, 0.50752, 0.75719, 0.92686, 0.99834, 0.96382, 0.82671, 0.6014, 0.31173, -0.011493, -0.33373, -0.62042, -0.84069, -0.97069, -0.99621, -0.91425, -0.73343, -0.47307, -0.16124, 0.16833, 0.47985, 0.73934, 0.91837, 0.99721, 0.96699, 0.83078, 0.60328, 0.3093, -0.018955, -0.34537, -0.63388, -0.8525, -0.97685, -0.99292, -0.89867, -0.70435, -0.43139, -0.11007, 0.2238, 0.53285, 0.78238, 0.94423, 1, 0.94316, 0.77988, 0.52836, 0.21686, -0.11944, -0.44243, -0.71536, -0.90706, -0.99547, -0.97028, -0.8341, -0.60229, -0.30122, 0.034667, 0.36681, 0.65694, 0.87146, 0.98543, 0.98541, 0.87118, 0.65572, 0.36387, 0.029436, -0.30867, -0.61097, -0.84203, -0.97463, -0.99298, -0.8947, -0.69107, -0.40586, -0.072555, 0.26957, 0.58003, 0.82196, 0.96649, 0.99622, 0.90736, 0.71026, 0.42819, 0.094721, -0.25033, -0.56561, -0.81317, -0.96308, -0.99709, -0.91082, -0.71444, -0.43147, -0.096023, 0.25132, 0.56836, 0.81645, 0.96521, 0.99625, 0.90552, 0.70388, 0.4158, 0.07647, -0.2725, -0.58817, -0.83153, -0.97237, -0.99305, -0.89077, -0.67793, -0.38073, -0.035975, 0.31351, 0.62412, 0.85698, 0.98279, 0.98556, 0.86468, 0.63506, 0.32538, -0.025512, -0.37343, -0.6744, -0.89024, -0.9934, -0.97056, -0.82435, -0.57312, -0.24867, 0.10774, 0.45062, 0.73607, 0.92735, 0.99973, 0.94364, 0.76604, 0.48957, 0.14971, -0.20975, -0.54229, -0.80475, -0.96292, -0.996, -0.89944, -0.68554, -0.38199, -0.028289, 0.32935, 0.64406, 0.87441, 0.98995, 0.97523, 0.83191, 0.57866, 0.24872, -0.11437, -0.46254, -0.74953, -0.93706, -0.99995, -0.92955, -0.735, -0.44205, -0.089718, 0.27488, 0.60282, 0.84993, 0.98278, 0.98322, 0.85092, 0.60349, 0.2742, -0.092485, -0.44686, -0.74072, -0.93394, -0.99998, -0.92957, -0.73204, -0.43418, -0.076615, 0.29171, 0.62018, 0.86351, 0.98799, 0.9762, 0.8295, 0.56791, 0.22745, -0.1448, -0.49717, -0.7806, -0.95544, -0.9971, -0.89948, -0.67595, -0.35758, 0.011154, 0.37857, 0.69297, 0.90997, 0.99876, 0.94656, 0.76045, 0.46653, 0.10626, -0.26933, -0.60687, -0.85822, -0.98738, -0.97565, -0.82444, -0.55512, -0.20605, 0.17282, 0.5271, 0.80574, 0.96845, 0.99151, 0.87132, 0.62498, 0.28793, -0.091102, -0.45714, -0.75692, -0.94665, -0.99842, -0.90442, -0.67809, -0.35233, 0.025302, 0.39946, 0.71513, 0.92572, 1, 0.92672, 0.71644, 0.39999, 0.024075, -0.35565, -0.68278, -0.90854, -0.99909, -0.94068, -0.74174, -0.43174, -0.056887, 0.32673, 0.66153, 0.89709, 0.99775, 0.94808, 0.75529, 0.44823, 0.073151, -0.31324, -0.65236, -0.89258, -0.99718, -0.94996, -0.75783, -0.44986, -0.07291, 0.31544, 0.65567, 0.89547, 0.99778, 0.9466, 0.74952, 0.43667, 0.056161, -0.33327, -0.67132, -0.90546, -0.99914, -0.93751, -0.72988, -0.40834, -0.022864, 0.36642, 0.69861, 0.92153, 1, 0.92143, 0.69787, 0.36426, -0.026998, -0.41423, -0.73621, -0.94184, -0.99831, -0.89638, -0.65194, -0.30363, 0.093273, 0.47553, 0.78208, 0.96374, 0.99119, 0.85975, 0.59022, 0.22565, -0.17545, -0.54852, -0.83332, -0.98367, -0.97501, -0.80844, -0.51068, -0.12982, 0.27236, 0.63046, 0.88603, 0.99717, 0.94546, 0.73906, 0.41148, 0.016226, -0.38193, -0.71753, -0.93522, -0.9989, -0.89778, -0.64826, -0.29135, 0.11403, 0.50074, 0.80453, 0.97475, 0.9828, 0.82706, 0.5332, 0.15009, -0.25836, -0.62378, -0.8848, -0.99741, -0.9424, -0.72873, -0.39212, 0.010863, 0.41224, 0.74413, 0.95017, 0.99522, 0.87132, 0.59923, 0.22499, -0.1878, -0.5688, -0.85285, -0.9912, -0.9599, -0.764, -0.43683, -0.034393, 0.3742, 0.71855, 0.93916, 0.99768, 0.88372, 0.6167, 0.24261, -0.17379, -0.56024, -0.84944, -0.99085, -0.95954, -0.76067, -0.4287, -0.021507, 0.38969, 0.73276, 0.9473, 0.99535, 0.86816, 0.58784, 0.2036, -0.21685, -0.59917, -0.87556, -0.99682, -0.94113, -0.71809, -0.3671, 0.049515, 0.45753, 0.78405, 0.97052, 0.9833, 0.8198, 0.50905, 0.10665, -0.31518, -0.68046, -0.92323, -0.99943, -0.89498, -0.62849, -0.24795, 0.17778, 0.57147, 0.86145, 0.99474, 0.94678, 0.72599, 0.37243, -0.049467, -0.46254, -0.79103, -0.97447, -0.97891, -0.80322, -0.47944, -0.067044, 0.35796, 0.71702, 0.94356, 0.99536, 0.86251, 0.56937, 0.17018, -0.26092, -0.64362, -0.90642, -1, -0.90657, -0.6433, -0.25927, 0.17365, 0.57417, 0.86688, 0.99645, 0.93816, 0.70271, 0.33429, -0.097599, -0.51123, -0.82812, -0.98791, -0.95995, -0.74925, -0.39565, 0.033634, 0.45673, 0.79271, 0.97711, 0.97433, 0.78457, 0.44398, 0.017809, -0.41202, -0.76266, -0.96629, -0.98332, -0.81015, -0.47998, -0.056564, 0.37806, 0.73945, 0.95717, 0.98857, 0.82722, 0.50429, 0.082621, -0.35544, -0.72411, -0.95097, -0.99124, -0.83669, -0.51741, -0.096023, 0.34453, 0.71729, 0.94846, 0.99204, 0.8391, 0.51964, 0.096811, -0.34549, -0.71926, -0.94992, -0.99121, -0.83458, -0.51101, -0.084987, 0.35829, 0.72993, 0.95518, 0.9885, 0.82288, 0.49134, 0.060515, -0.38276, -0.74889, -0.96362, -0.9832, -0.80333, -0.46022, -0.023348, 0.4185, 0.77531, 0.97414, 0.97412, 0.77494, 0.41704, -0.026513, -0.46486, -0.80796, -0.98514, -0.95963, -0.73638, -0.36111, 0.08893, 0.52082, 0.84515, 0.99453, 0.93769, 0.68611, 0.29177, -0.16351, -0.58495, -0.88461, -0.99971, -0.9059, -0.62247, -0.20848, 0.24944, 0.65523, 0.92351, 0.9976, 0.86159, 0.54382, 0.11108, -0.34537, -0.72897, -0.95838, -0.98469, -0.802, -0.44879, 7.2692e-05, 0.44915, 0.80271, 0.98512, 0.95715, 0.72443, 0.33648, -0.1238, -0.55775, -0.8721, -0.99907, -0.911, -0.62655, -0.20678, 0.25788, 0.66701, 0.93197, 0.99511, 0.84242, 0.50671, 0.060676, -0.39879, -0.77161, -0.97633, -0.96799, -0.74807, -0.36436, 0.099351, 0.54151, 0.86503, 0.99863, 0.91262, 0.62558, 0.2005, -0.26905, -0.6794, -0.93969, -0.99207, -0.82461, -0.4741, -0.018115, 0.44212, 0.80419, 0.98724, 0.95018, 0.70094, 0.29489, -0.17735, -0.6101, -0.90625, -0.99912, -0.86751, -0.54069, -0.091938, 0.37776, 0.76244, 0.97507, 0.96729, 0.74053, 0.34583, -0.12752, -0.57214, -0.88692, -1, -0.88531, -0.56867, -0.12204, 0.35271, 0.74699, 0.97039, 0.97143, 0.74952, 0.35531, -0.12082, -0.56936, -0.8869, -0.99999, -0.88219, -0.56037, -0.10872, 0.36835, 0.76022, 0.9758, 0.96471, 0.72918, 0.32373, -0.15738, -0.60201, -0.90621, -0.99865, -0.85736, -0.51509, -0.051822, 0.42384, 0.8001, 0.98827, 0.94375, 0.67668, 0.24975, -0.23639, -0.66685, -0.93964, -0.98985, -0.80523, -0.42929, 0.048902, 0.51568, 0.85983, 0.9991, 0.89992, 0.58564, 0.13112, -0.35502, -0.75629, -0.97628, -0.96188, -0.7162, -0.29802, 0.19217, 0.63624, 0.92692, 0.99373, 0.82017, 0.44791, -0.033101, -0.50631, -0.85676, -0.99905, -0.89825, -0.57854, -0.11758, 0.37231, 0.77144, 0.98203, 0.95225, 0.68902, 0.2566, -0.23906, -0.67617, -0.94706, -0.98476, -0.77961, -0.3819, 0.11037, 0.57557, 0.89847, 0.99882, 0.85139, 0.49244, 0.010895, -0.47359, -0.84032, -0.99768, -0.90608, -0.58804, -0.12277, 0.37345, 0.77631, 0.98461, 0.94575, 0.66915, 0.22405, -0.27769, -0.70965, -0.96272, -0.9727, -0.73671, -0.31413, 0.18823, 0.64309, 0.93489, 0.98924, 0.79196, 0.39293, -0.10644, -0.57888, -0.9037, -0.99764, -0.83632, -0.4607, 0.033214, 0.51883, 0.87138, 1, 0.87126, 0.51795, 0.030889, -0.46437, -0.83985, -0.99823, -0.8982, -0.56531, -0.085599, 0.41657, 0.81067, 0.994, 0.9185, 0.60349, 0.13083, -0.37622, -0.78512, -0.9887, -0.93333, -0.63315, -0.16663, 0.34389, 0.76419, 0.98348, 0.94368, 0.65491, 0.19308, -0.31999, -0.74863, -0.97921, -0.95032, -0.66926, -0.21031, 0.30475, 0.73895, 0.9765, 0.9538, 0.67653, 0.21841, -0.29833, -0.73544, -0.97571, -0.95443, -0.67693, -0.21742, 0.30078, 0.73823, 0.97694, 0.95225, 0.67045, 0.20733, -0.31208, -0.74722, -0.98003, -0.94707, -0.65692, -0.18809, 0.33213, 0.76213, 0.98457, 0.93847, 0.63604, 0.1596, -0.36072, -0.78247, -0.98989, -0.92575, -0.60731, -0.12175, 0.39753, 0.80754, 0.99506, 0.90804, 0.57014, 0.07444, -0.44208, -0.83636, -0.99888, -0.88423, -0.52387, -0.017663, 0.49368, 0.8677, 0.9999, 0.85305, 0.46779, -0.048466, -0.55138, -0.90001, -0.99639, -0.81309, -0.40125, 0.12362, 0.61392, 0.93144, 0.98639, 0.7629, 0.32373, -0.20719, -0.67968, -0.95978, -0.96777, -0.70102, -0.23494, 0.29816, 0.7466, 0.98249, 0.93824, 0.62611, 0.13493, -0.39506, -0.81217, -0.99673, -0.89547, -0.53709, -0.02422, 0.49584, 0.87337, 0.99939, 0.83721, 0.43327, -0.096055, -0.59782, -0.92673, -0.98724, -0.76145, -0.3145, 0.22402, 0.69761, 0.96833, 0.95701, 0.66658, 0.18144, -0.35692, -0.79112, -0.99393, -0.90561, -0.55167, -0.035652, 0.49106, 0.87359, 0.99915, 0.8304, 0.41671, -0.12014, -0.62173, -0.93969, -0.97967, -0.72946, -0.26285, 0.28196, 0.74324, 0.98374, 0.93159, 0.60194, 0.092694, -0.44443, -0.84903, -1, -0.8518, -0.44845, 0.089509, 0.60083, 0.93194, 0.98306, 0.73845, 0.27137, -0.27769, -0.74321, -0.98451, -0.92838, -0.5914, -0.075229, 0.46396, 0.86264, 0.99961, 0.8329, 0.4128, -0.13312, -0.63871, -0.94974, -0.971, -0.69563, -0.20744, 0.34444, 0.79104, 0.99528, 0.89418, 0.51839, -0.01692, -0.54725, -0.90925, -0.99112, -0.76724, -0.30633, 0.24944, 0.72828, 0.98181, 0.93117, 0.59168, 0.06835, -0.47643, -0.87325, -0.9984, -0.81253, -0.37319, 0.1828, 0.68192, 0.96801, 0.95128, 0.63656, 0.12215, -0.4308, -0.84868, -0.99994, -0.83666, -0.40984, 0.14632, 0.65655, 0.95968, 0.95967, 0.65611, 0.14472, -0.41273, -0.83951, -0.99999, -0.84285, -0.41764, 0.14066, 0.65438, 0.95968, 0.95887, 0.65183, 0.13631, -0.42303, -0.84713, -0.9999, -0.83199, -0.3969, 0.16591, 0.67558, 0.96799, 0.94864, 0.62339, 0.096811, -0.46125, -0.87041, -0.99807, -0.80254, -0.34679, 0.22165, 0.71835, 0.98178, 0.92596, 0.56865, 0.025916, -0.52549, -0.90558, -0.98998, -0.75071, -0.26566, 0.30659, 0.77856, 0.99526, 0.8852, 0.48415, -0.076325, -0.61189, -0.94603, -0.96828, -0.67091, -0.15177, 0.41777, 0.84926, 0.99962, 0.81863, 0.36594, -0.20845, -0.71378, -0.98186, -0.92313, -0.55676, -0.0045473, 0.54939, 0.92, 0.98312, 0.71725, 0.21102, -0.36614, -0.82068, -0.9998, -0.84295, -0.40249, 0.17365, 0.6915, 0.97634, 0.93175, 0.57237, 0.019327, -0.54047, -0.91743, -0.98355, -0.71602, -0.20526, 0.37545, 0.82869, 1, 0.83067, 0.37803, -0.2038, -0.71624, -0.98403, -0.91523, -0.533, 0.032052, 0.58632, 0.93952, 0.97004, 0.66699, 0.13426, -0.44492, -0.87093, -0.99659, -0.77815, -0.29067, 0.29754, 0.78296, 0.99728, 0.86583, 0.43382, -0.14905, -0.68032, -0.97507, -0.93037, -0.56139, 0.0035619, 0.56748, 0.93333, 0.97293, 0.67203, 0.1356, -0.44856, -0.87556, -0.99525, -0.7652, -0.26591, 0.32719, 0.80522, 0.9995, 0.8411, 0.38555, -0.20646, -0.72565, -0.98808, -0.90045, -0.49341, 0.08893, 0.63989, 0.96354, 0.94442, 0.58894, 0.023397, -0.5507, -0.92839, -0.97448, -0.67209, -0.12902, 0.46046, 0.88508, 0.99229, 0.7432, 0.22691, -0.37118, -0.83588, -0.99959, -0.80293, -0.31643, 0.28453, 0.78286, 0.99817, 0.85215, 0.3973, -0.20181, -0.72789, -0.98975, -0.89189, -0.46948, 0.12404, 0.67258, 0.976, 0.92326, 0.5332, -0.051951, -0.61831, -0.95844, -0.94739, -0.58882, -0.013948, 0.56626, 0.93845, 0.96542, 0.63681, 0.073345, -0.51738, -0.9173, -0.97841, -0.67771, -0.12607, 0.47246, 0.89607, 0.98738, 0.71209, 0.17209, -0.43211, -0.8757, -0.99323, -0.74052, -0.21142, 0.39682, 0.85698, 0.99679, 0.76353, 0.24415, -0.36695, -0.84058, -0.99873, -0.78158, -0.27039, 0.34277, 0.82702, 0.99963, 0.79511, 0.29025, -0.32448, -0.81671, -0.99995, -0.80443, -0.30384, 0.31222, 0.80993, 1, 0.80978, 0.31123, -0.30606, -0.80688, -0.99999, -0.8113, -0.31248, 0.30604, 0.80763, 1, 0.80904, 0.30758, -0.31217, -0.81217, -0.99997, -0.80293, -0.29651, 0.32441, 0.82037, 0.99973, 0.79281, 0.2792, -0.34267, -0.83201, -0.99898, -0.77843, -0.25555, 0.36681, 0.84675, 0.9973, 0.75944, 0.22546, -0.39666, -0.86413, -0.99413, -0.73542, -0.18882, 0.43193, 0.88359, 0.9888, 0.70587, 0.14552, -0.47225, -0.9044, -0.98055, -0.67027, -0.095508, 0.51715, 0.92572, 0.96846, 0.62803, 0.0388, -0.56601, -0.94655, -0.95155, -0.57859, 0.024495, 0.61804, 0.96574, 0.92876, 0.52143, -0.094126, -0.6723, -0.98197, -0.89897, -0.45608, 0.16967, 0.72761, 0.99381, 0.86104, 0.38221, -0.25049, -0.78259, -0.99966, -0.81384, -0.29965, 0.33569, 0.83562, 0.99785, 0.75634, 0.20847, -0.42409, -0.88485, -0.98661, -0.68763, -0.10901, 0.51416, 0.9282, 0.96417, 0.60699, 0.0019788, -0.60404, -0.96339, -0.9288, -0.51401, 0.11149, 0.6915, 0.98799, 0.87892, 0.40865, -0.22982, -0.77393, -0.99948, -0.81315, -0.29135, 0.35089, 0.84839, 0.99532, 0.73049, 0.16311, -0.47205, -0.91167, -0.97309, -0.6304, -0.025577, 0.59009, 0.96033, 0.93064, 0.51297, -0.11886, -0.70127, -0.99086, -0.86621, -0.37903, 0.26704, 0.80141, 0.99983, 0.77864, 0.23031, -0.41502, -0.88597, -0.98408, -0.6676, -0.069526, 0.55808, 0.95024, 0.94095, 0.53372, -0.099528, -0.69083, -0.98951, -0.86855, -0.37885, 0.27196, 0.80736, 0.99944, 0.76604, 0.20616, -0.44182, -0.90147, -0.9763, -0.6339, -0.02028, 0.60223, 0.96698, 0.91736, 0.4742, -0.17268, -0.74558, -0.99812, -0.8213, -0.29079, 0.36519, 0.86391, 0.98999, 0.68858, 0.089477, -0.54852, -0.94931, -0.93905, -0.52175, 0.12202, 0.71302, 0.99447, 0.84366, 0.32575, -0.33415, -0.84868, -0.99331, -0.70456, -0.10804, 0.53588, 0.94566, 0.9417, 0.52531, -0.12146, -0.71516, -0.99514, -0.83809, -0.31257, 0.35062, 0.85967, 0.99015, 0.68414, 0.07618, -0.56561, -0.95769, -0.92642, -0.48521, 0.17101, 0.75165, 0.99915, 0.8033, 0.25063, -0.41361, -0.8941, -0.97679, -0.62441, 0.0064049, 0.63457, 0.97966, 0.88722, 0.39813, -0.26924, -0.81628, -0.99777, -0.73197, -0.13767, 0.51864, 0.94212, 0.94209, 0.51814, -0.13927, -0.73413, -0.99811, -0.81174, -0.25871, 0.41142, 0.89568, 0.97471, 0.61229, -0.027773, -0.65544, -0.98579, -0.86846, -0.35632, 0.31807, 0.84788, 0.9916, 0.68331, 0.063192, -0.58597, -0.96763, -0.907, -0.43139, 0.24189, 0.80455, 0.99849, 0.73434, 0.13291, -0.52977, -0.94906, -0.93175, -0.48538, 0.1848, 0.76998, 1, 0.76828, 0.18144, -0.4895, -0.9341, -0.94623, -0.51981, 0.14782, 0.74705, 0.9995, 0.78746, 0.2091, -0.46677, -0.92544, -0.95288, -0.53587, 0.13141, 0.73751, 0.99905, 0.79334, 0.2162, -0.4624, -0.92444, -0.95297, -0.53415, 0.13573, 0.74201, 0.9994, 0.78639, 0.20284, -0.47652, -0.93127, -0.94651, -0.51459, 0.16075, 0.76024, 0.99999, 0.76604, 0.16884, -0.50865, -0.94485, -0.93228, -0.47646, 0.20619, 0.79088, 0.99893, 0.73077, 0.11387, -0.55759, -0.96284, -0.90786, -0.41849, 0.2714, 0.83146, 0.99304, 0.6782, 0.037637, -0.6212, -0.98156, -0.86975, -0.33914, 0.35503, 0.87824, 0.97786, 0.60537, -0.059725, -0.69614, -0.99593, -0.8136, -0.23702, 0.45466, 0.92594, 0.9478, 0.50916, -0.17716, -0.7775, -0.99947, -0.73454, -0.11141, 0.56626, 0.96767, 0.89643, 0.38689, -0.31208, -0.85847, -0.98444, -0.62781, 0.036927, 0.68373, 0.99483, 0.81694, 0.23709, -0.45964, -0.93015, -0.94225, -0.48953, 0.20499, 0.79845, 0.99738, 0.70299, 0.060515, -0.61212, -0.98153, -0.86417, -0.31781, 0.38657, 0.899, 0.96442, 0.54983, -0.13876, -0.75838, -0.99993, -0.74247, -0.11411, 0.57147, 0.97143, 0.88534, 0.35582, -0.35218, -0.88376, -0.97199, -0.57212, 0.11538, 0.74505, 0.99999, 0.75144, 0.12417, -0.5659, -0.97062, -0.88536, -0.35272, 0.35841, 0.88841, 0.96875, 0.5583, -0.13533, -0.7605, -0.99976, -0.73118, -0.090812, 0.59594, 0.97952, 0.86424, 0.30838, -0.40495, -0.91177, -0.95303, -0.50717, 0.19822, 0.80236, 0.99573, 0.67884, 0.013706, -0.65865, -0.99295, -0.81708, -0.22098, 0.48901, 0.94739, 0.91773, 0.41486, -0.30221, -0.86345, -0.97874, -0.58804, 0.107, 0.74684, 0.99996, 0.73477, 0.088351, -0.60412, -0.98298, -0.85101, -0.27636, 0.4423, 0.93079, 0.93432, 0.45057, -0.26846, -0.84749, -0.98381, -0.60571, 0.089445, 0.73797, 1, 0.73779, 0.088415, -0.60756, -0.98455, -0.84405, -0.25949, 0.46184, 0.94008, 0.92294, 0.41897, -0.30627, -0.86996, -0.97402, -0.56299, 0.14609, 0.77801, 0.99783, 0.6886, 0.0139, -0.66837, -0.99571, -0.79376, -0.16946, 0.54523, 0.96967, 0.87732, 0.31697, -0.41273, -0.92218, -0.93889, -0.45346, 0.27479, 0.85608, 0.97878, 0.57665, -0.13501, -0.77438, -0.99786, -0.6849, -0.0033842, 0.68014, 0.99746, 0.77718, 0.13762, -0.5764, -0.9793, -0.85302, -0.26538, 0.46604, 0.94529, 0.91243, 0.38481, -0.35175, -0.89753, -0.95583, -0.49451, 0.23596, 0.83819, 0.984, 0.5935, -0.12082, -0.76939, -0.99795, -0.6812, 0.0081495, 0.69322, 0.99891, 0.75736, 0.10051, -0.61166, -0.98824, -0.82204, -0.20393, 0.52652, 0.96737, 0.87555, 0.30118, -0.43944, -0.93777, -0.9184, -0.39158, 0.35189, 0.90088, 0.95128, 0.47471, -0.26513, -0.85811, -0.97496, -0.55035, 0.18023, 0.81078, 0.99033, 0.61848, -0.098081, -0.76015, -0.9983, -0.67921, 0.019391, 0.70734, 0.99983, 0.7328, 0.05529, -0.65339, -0.99585, -0.77961, -0.12556, 0.5992, 0.98729, 0.82006, 0.19115, -0.5456, -0.97502, -0.85464, -0.2519, 0.49326, 0.95987, 0.88386, 0.30773, -0.44278, -0.94264, -0.90827, -0.35867, 0.39466, 0.92403, 0.92839, 0.40478, -0.3493, -0.90469, -0.94476, -0.44619, 0.30706, 0.88522, 0.95788, 0.48305, -0.26818, -0.86613, -0.96823, -0.51553, 0.23289, 0.84788, 0.97625, 0.54382, -0.20134, -0.83088, -0.98236, -0.56811, 0.17365, 0.81546, 0.98691, 0.58857, -0.14991, -0.80193, -0.99022, -0.60538, 0.13019, 0.7905, 0.99255, 0.61868, -0.11453, -0.78138, -0.99413, -0.62859, 0.10295, 0.77472, 0.99512, 0.63521, -0.095476, -0.7706, -0.99564, -0.63861, 0.092115, 0.76911, 0.99576, 0.63883, -0.092871, -0.77025, -0.9955, -0.63585, 0.097743, 0.77402, 0.99481, 0.62966, -0.10673, -0.78035, -0.99362, -0.62019, 0.11981, 0.78915, 0.99179, 0.60735, -0.13696, -0.80028, -0.98913, -0.59101, 0.15816, 0.81355, 0.9854, 0.57104, -0.18336, -0.82873, -0.98032, -0.54727, 0.21248, 0.84554, 0.97355, 0.51952, -0.24542, -0.86365, -0.96472, -0.48761, 0.28205, 0.88265, 0.9534, 0.45135, -0.32219, -0.9021, -0.93915, -0.41055, 0.36561, 0.92149, 0.92146, 0.36507, -0.41202, -0.94023, -0.89983, -0.31478, 0.46106, 0.95769, 0.87372, 0.2596, -0.51229, -0.97315, -0.84259, -0.1995, 0.56517, 0.98585, 0.80591, 0.13455, -0.61907, -0.99496, -0.76318, -0.064884, 0.67327, 0.9996, 0.71393, -0.0092317, -0.72691, -0.99885, -0.65775, 0.087417, 0.77905, 0.99179, 0.59433, -0.16914, -0.82861, -0.97746, -0.52345, 0.25372, 0.87441, 0.95493, 0.44505, -0.3403, -0.91518, -0.92333, -0.35921, 0.42781, 0.94956, 0.88184, 0.26623, -0.51503, -0.97612, -0.82978, -0.16663, 0.60052, 0.99342, 0.7666, 0.06116, -0.68266, -0.99998, -0.69198, 0.049128, 0.75965, 0.9944, 0.6058, -0.1629, -0.82955, -0.97537, -0.50829, 0.27851, 0.89027, 0.94171, 0.39996, -0.39402, -0.93969, -0.89251, -0.28175, 0.50716, 0.97565, 0.82709, 0.15501, -0.61541, -0.99603, -0.74519, -0.021507, 0.71601, 0.99889, 0.64697, -0.11649, -0.806, -0.98249, -0.53308, 0.25626, 0.88233, 0.94543, 0.40478, -0.39464, -0.94194, -0.88677, -0.26394, 0.52808, 0.98186, 0.80612, 0.1131, -0.6527, -0.99939, -0.70378, 0.044545, 0.76438, 0.99219, 0.58079, -0.20513, -0.85892, -0.95848, -0.43907, 0.3642, 0.93217, 0.8972, 0.28143, -0.51678, -0.98025, -0.80815, -0.11164, 0.65752, 0.99972, 0.69218, -0.065626, -0.7809, -0.98786, -0.55124, 0.24484, 0.8814, 0.94284, 0.38856, -0.41975, -0.95381, -0.864, -0.20861, 0.58357, 0.99349, 0.75204, 0.017098, -0.72924, -0.99672, -0.60915, 0.17912, 0.84971, 0.96099, 0.4392, -0.37223, -0.93834, -0.88531, -0.24767, 0.55376, 0.98929, 0.77051, 0.041705, -0.71489, -0.99795, -0.6194, 0.17015, 0.84694, 0.96136, 0.43693, -0.37816, -0.94184, -0.87865, -0.23017, 0.57186, 0.99272, 0.75127, 0.0081818, -0.74054, -0.99445, -0.58332, 0.21825, 0.87388, 0.94419, 0.38158, -0.43709, -0.96261, -0.84186, -0.15544, 0.63564, 0.99932, 0.69049, -0.08341, -0.80129, -0.97909, -0.49644, 0.32141, 0.92238, 0.90018, 0.26932, -0.54389, -0.98911, -0.7645, -0.021733, 0.73593, 0.99446, 0.57794, -0.23124, -0.88344, -0.93503, -0.35042, 0.47289, 0.97426, 0.81171, 0.095557, -0.68596, -0.99932, -0.63007, 0.16992, 0.85389, 0.95369, 0.40052, -0.42716, -0.96222, -0.83745, -0.13796, 0.65643};
    public double [] sound = shortchirp;

    private int attemptNumber = 0;
    private final static int MAXATTEMPTS = 15;
    private int adjustAmount = 250;
    boolean finetune = false;
    String message = "";
    int numFound = -1;

    static { System.loadLibrary("MyLib"); }

    public AutoTuner(MainActivity context, CountdownBuffer buffer, Recorder recorder, Player player) {
        this.mainActivity = context;
        this.buffer = buffer;
        this.recorder = recorder;
        this.player = player;
    }

    Runnable tuneRoutine = new Runnable () {
        @Override
        public void run() {
            try {
                Thread.sleep(250);
                buffer.getAll();
            } catch (Exception e) {}

            if (finetune) mainActivity.addInfo("" + attemptNumber + " ~ fine tuning", 50);
            else if (numFound != -1) mainActivity.addInfo("Attempt " + attemptNumber + " (" + numFound + ")", 50);
            else mainActivity.addInfo("Attempt " + attemptNumber, 50);

            int store_for = Math.max(1, (int)Math.ceil((C.CHIRPSPACE*2 + player.sound.length*2)/44100.0));
            buffer.storeData(store_for);
            Log.v(TAG, "Buffer storing. Buffer has " + buffer.howMany() + " and buffer wants " + buffer.doYouWant());

            while (buffer.doYouWant()) {
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Log.v(TAG, "" + attemptNumber + " - Converting");
            ArrayList<Byte> tmpBuffer = buffer.getAll();
            Object[] channels = AudioFuncs.pcmToDouble(tmpBuffer);

            double [] signal = (double[])channels[0];

            //Log.v(TAG, "" + attemptNumber + " - JNI Call " + tmpBuffer.size());
            int [] peaks = new int[200];
            double [] xcorrOutput = new double [signal.length];
            numFound = findchirps(signal, sound, xcorrOutput, peaks, signal.length, sound.length, xcorrOutput.length, peaks.length);
            Log.v(TAG, "Result - " + message + " (" + numFound + ")");


            int IDEAL = (int)(2*44100.0/(player.sound.length + C.CHIRPSPACE));
            boolean rerun = false;

            //if (!finetune) { // Once in finetune, we stay there.
            if (numFound > (IDEAL - 2) && numFound < (IDEAL + 2)) {
                finetune = true;
            } else if (numFound > (IDEAL/2 - 2) && numFound < (IDEAL/2 + 2)) {
                Log.v(TAG, "Overlapped");
                adjustAmount = (player.sound.length + C.CHIRPSPACE)/4;
                rerun = true;
                finetune = false;
            }
            else {
                adjustAmount = 300;
                rerun = true;
                finetune = false;
            }
            //}

            if (finetune) {
                double [] leftmic = (double[])channels[1];
                int [] leftPeaks = new int[200];
                double [] leftXcorr = new double [leftmic.length];
                findchirps(leftmic, sound, leftXcorr, leftPeaks, leftmic.length, sound.length, leftXcorr.length, leftPeaks.length);

                ArrayList<Integer> watch_to_phone = new ArrayList<Integer>();
                ArrayList<Integer> phone_to_watch = new ArrayList<Integer>();

                for (int i = 1; i < numFound; i++) {
                    int delta = peaks[i] - peaks[i-1];
                    if (delta < 0) continue;
                    if (Math.abs(leftXcorr[peaks[i-1]]) < Math.abs(leftXcorr[peaks[i]])) watch_to_phone.add(delta);
                    else phone_to_watch.add(delta);
                }

                Log.v(TAG, "Watch to phone: " + watch_to_phone.toString());
                Log.v(TAG, "Phone to watch: " + phone_to_watch.toString());

                double w_2_p = get_average(watch_to_phone);
                double p_2_w = get_average(phone_to_watch);

                if (Math.abs(w_2_p - p_2_w) > 50) {
                    adjustAmount = (int)((p_2_w - w_2_p)/2);
                    Log.v(TAG, "Adjust amount: " + adjustAmount);
                    rerun = true;
                }
            }

            if (rerun) {
                player.tweak(adjustAmount);
                if (!finetune) attemptNumber++;
                if (attemptNumber > MAXATTEMPTS) {
                    //mainActivity.addInfo("Autotuned failed after " + (attemptNumber - 1) + " attempts.", 250);
                    mainActivity.doneAutotune(false);
                } else {
                    try {
                        Thread.currentThread().sleep(250);
                        tuneRoutine.run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                mainActivity.doneAutotune(true);
            }

            //mainActivity.addInfo("Result - " + message + " (" + numFound + ")", 50);
            //filename = file.getAbsolutePath() + "/autotuned." + System.currentTimeMillis() + Recorder.AUDIO_RECORDER_FILE_EXT_WAV;
            //AudioFile.SaveFromBuffer(tmpBuffer, filename, recorder.bufferSize);



        }
    };

    double get_average (ArrayList<Integer> arr) {
        double sum = 0;
        for (int i = 0; i < arr.size(); i++) {
            sum += arr.get(i);
        }

        return sum / arr.size();
    }

    public void start () {
        attemptNumber = 1;
        finetune = false;
        message = "";
        numFound = -1;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Log.e(TAG, "Starting auto tuner");
                new Thread(tuneRoutine).start();
            }
        }, 1000);
    }


    public native int findchirps (double [] signal, double [] chirp, double [] xcorr, int [] windows, int signallen, int chirplen, int corrlen, int windowlen);
}







