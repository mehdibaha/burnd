package android.support.design.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.insa.burnd.utils.Utils;

import java.util.List;

/**
 * A {@link com.getbase.floatingactionbutton.FloatingActionsMenu} subclass that works with the design support library's {@link CoordinatorLayout}
 */
@CoordinatorLayout.DefaultBehavior(SupportFloatingActionsMenu.Behavior.class)
public class SupportFloatingActionsMenu extends com.getbase.floatingactionbutton.FloatingActionsMenu {

    public SupportFloatingActionsMenu(Context context) {
        super(context);
    }

    public SupportFloatingActionsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SupportFloatingActionsMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static class Behavior extends android.support.design.widget.CoordinatorLayout.Behavior<SupportFloatingActionsMenu> {
        private static final String TAG = "Behav";
        private float mTranslationY;
        private int toolbarHeight;

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.toolbarHeight = Utils.getToolbarHeight(context);
        }

        public boolean layoutDependsOn(CoordinatorLayout parent, SupportFloatingActionsMenu child, View dependency) {
            return super.layoutDependsOn(parent, child, dependency) || (dependency instanceof AppBarLayout);
        }
        /*
        * distToScroll = f(dependencyY) = -2*dependencyY
        * The fab to translates two times the y coordinate the user scrolls (empirical law).
        * */
        public boolean onDependentViewChanged(CoordinatorLayout parent, SupportFloatingActionsMenu child, View dependency) {
            if(dependency instanceof Snackbar.SnackbarLayout) {
                this.updateFabTranslationForSnackbar(parent, child, dependency);
            } else if(dependency instanceof AppBarLayout) {
                float distToScroll = -2*dependency.getY();
                child.setTranslationY(distToScroll);
            }

            return false;
        }

        private void updateFabTranslationForSnackbar(CoordinatorLayout parent, SupportFloatingActionsMenu fab, View snackbar) {
            float translationY = this.getFabTranslationYForSnackbar(parent, fab);
            if(translationY != this.mTranslationY) {
                ViewCompat.animate(fab).cancel();
                if(Math.abs(translationY - this.mTranslationY) == (float)snackbar.getHeight()) {
                    ViewCompat.animate(fab).translationY(translationY).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).setListener(null);
                } else {
                    ViewCompat.setTranslationY(fab, translationY);
                }

                this.mTranslationY = translationY;
            }

        }

        private float getFabTranslationYForSnackbar(CoordinatorLayout parent, SupportFloatingActionsMenu fab) {
            float minOffset = 0.0F;
            List dependencies = parent.getDependencies(fab);
            int i = 0;

            for(int z = dependencies.size(); i < z; ++i) {
                View view = (View)dependencies.get(i);
                if(view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(fab, view)) {
                    minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - (float)view.getHeight());
                }
            }

            return minOffset;
        }
    }
}