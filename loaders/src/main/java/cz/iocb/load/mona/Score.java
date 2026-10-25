package cz.iocb.load.mona;



/**
 * The quality score of a spectrum in the JSON export of MoNA.
 */
public class Score
{
    /**
     * The impacts on the score.
     */
    Impact[] impacts;

    /**
     * The relative score.
     */
    double relativeScore;

    /**
     * The scaled score.
     */
    double scaledScore;

    /**
     * The score.
     */
    double score;


    /**
     * Creates an empty score.
     */
    public Score()
    {
    }
}
