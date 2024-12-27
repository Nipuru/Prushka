import { connect } from "react-redux";
import "./index.less";

const LayoutFooter = (props: any) => {
	const { themeConfig } = props;
	return (
		<>
			{!themeConfig.footer && (
				<div className="footer">
					<a href="http://www.spicyboy.cn/" target="_blank" rel="noreferrer">
                        Prushka-Admin 2024 © Create By Nipuru.
					</a>
				</div>
			)}
		</>
	);
};

const mapStateToProps = (state: any) => state.global;
export default connect(mapStateToProps)(LayoutFooter);
